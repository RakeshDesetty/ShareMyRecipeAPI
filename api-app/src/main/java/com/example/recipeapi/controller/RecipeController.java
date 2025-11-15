package com.example.recipeapi.controller;

import com.example.recipeapi.config.RabbitConfig;
import com.example.recipeapi.dto.RecipeDtos.CreateRequest;
import com.example.recipeapi.model.Chef;
import com.example.recipeapi.model.Recipe;
import com.example.recipeapi.repository.ChefRepository;
import com.example.recipeapi.repository.RecipeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.criteria.Predicate;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;

@RestController
@RequestMapping("/api")
public class RecipeController {

    private final ChefRepository chefRepo;
    private final RecipeRepository recipeRepo;
    private final AmqpTemplate amqp;
    private final Path tempDir;
    private final ObjectMapper mapper = new ObjectMapper();

    public RecipeController(ChefRepository chefRepo, RecipeRepository recipeRepo, AmqpTemplate amqp,
                            @Value("${app.upload.temp-dir}") String tempDir) {
        this.chefRepo = chefRepo;
        this.recipeRepo = recipeRepo;
        this.amqp = amqp;
        this.tempDir = Paths.get(tempDir);
    }

    // Public search with filters + pagination
    @GetMapping("/recipes")
    public ResponseEntity<?> search(@RequestParam(required=false) String q,
                                    @RequestParam(required=false) String published_from,
                                    @RequestParam(required=false) String published_to,
                                    @RequestParam(required=false) UUID chef_id,
                                    @RequestParam(required=false) String chef_handle,
                                    @RequestParam(required=false) String labels,
                                    @RequestParam(defaultValue="0") int page,
                                    @RequestParam(defaultValue="20") int page_size) {
        int size = Math.min(page_size, 100);
        Pageable pageable = PageRequest.of(Math.max(0, page), size, Sort.by(Sort.Direction.DESC, "publishedAt", "createdAt"));
        Specification<Recipe> spec = (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            preds.add(cb.equal(root.get("state"), Recipe.State.PUBLISHED));
            if (StringUtils.hasText(q)) {
                String like = "%" + q.toLowerCase() + "%";
                preds.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("summary")), like),
                        cb.like(cb.lower(root.get("ingredientsJson")), like),
                        cb.like(cb.lower(root.get("stepsJson")), like)
                ));
            }
            if (chef_id != null) preds.add(cb.equal(root.get("chef").get("id"), chef_id));
            if (StringUtils.hasText(chef_handle)) preds.add(cb.equal(root.get("chef").get("handle"), chef_handle));
            if (StringUtils.hasText(published_from)) {
                try { preds.add(cb.greaterThanOrEqualTo(root.get("publishedAt"), Instant.parse(published_from))); } catch (DateTimeParseException e) {}
            }
            if (StringUtils.hasText(published_to)) {
                try { preds.add(cb.lessThanOrEqualTo(root.get("publishedAt"), Instant.parse(published_to))); } catch (DateTimeParseException e) {}
            }
            if (StringUtils.hasText(labels)) {
                String[] parts = labels.split(",");
                for (String l: parts) preds.add(cb.isMember(l.trim(), root.get("labels")));
            }
            return cb.and(preds.toArray(new Predicate[0]));
        };
        Page<Recipe> p = recipeRepo.findAll(spec, pageable);
        Map<String,Object> meta = Map.of(
                "page", p.getNumber(),
                "page_size", p.getSize(),
                "total_items", p.getTotalElements(),
                "total_pages", p.getTotalPages(),
                "has_next", p.hasNext(),
                "has_prev", p.hasPrevious()
        );
        return ResponseEntity.ok(Map.of("data", p.getContent(), "meta", meta));
    }

    // Authenticated create recipe -> store minimal recipe as DRAFT and enqueue for worker
    @PostMapping(path="/chef/recipes", consumes = {"multipart/form-data"})
    public ResponseEntity<?> create(@RequestPart("recipe") CreateRequest req,
                                    @RequestPart(value="images", required=false) MultipartFile[] images,
                                    Authentication auth) throws Exception {
        if (auth == null) return ResponseEntity.status(401).body(Map.of("error","unauthenticated"));
        UUID uid = (UUID) auth.getPrincipal();
        Chef chef = chefRepo.findById(uid).orElseThrow();
        Recipe r = new Recipe();
        r.setChef(chef);
        r.setTitle(req.title);
        r.setSummary(req.summary);
        r.setIngredientsJson(req.ingredients != null ? mapper.writeValueAsString(req.ingredients) : "[]");
        r.setStepsJson(req.steps != null ? mapper.writeValueAsString(req.steps) : "[]");
        r.setLabels(req.labels != null ? req.labels : new ArrayList<>());
        r.setState(req.publish ? Recipe.State.PUBLISHED : Recipe.State.DRAFT);
        if (req.publish) r.setPublishedAt(Instant.now());
        recipeRepo.save(r);

        // store files to temp and build message
        String msgId = UUID.randomUUID().toString();
        List<String> tempPaths = new ArrayList<>();
        if (images != null && images.length>0) {
            Files.createDirectories(tempDir);
            int idx=0;
            for (MultipartFile mf: images) {
                String fname = msgId + "_" + (idx++) + "_" + mf.getOriginalFilename();
                Path p = tempDir.resolve(fname);
                mf.transferTo(p);
                tempPaths.add(p.toString());
            }
        }
        Map<String,Object> msg = new HashMap<>();
        msg.put("msgId", msgId);
        msg.put("externalRecipeId", r.getId().toString());
        msg.put("chefId", r.getChef().getId().toString());
        msg.put("title", r.getTitle());
        msg.put("summary", r.getSummary());
        msg.put("ingredientsJson", r.getIngredientsJson());
        msg.put("stepsJson", r.getStepsJson());
        msg.put("labels", r.getLabels());
        msg.put("publish", req.publish);
        msg.put("tempImagePaths", tempPaths);
        amqp.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, msg);

        return ResponseEntity.accepted().body(Map.of("id", r.getId(), "status","PROCESSING"));
    }

    // simple get by id
    @GetMapping("/recipes/{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        return recipeRepo.findById(id).map(r -> ResponseEntity.ok(r)).orElseGet(() -> ResponseEntity.status(404).body(Map.of("error","not_found")));
    }

    // Update and delete (ownership/admin checks)
    @PutMapping("/chef/recipes/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody CreateRequest req, Authentication auth) {
        if (auth==null) return ResponseEntity.status(401).body(Map.of("error","unauthenticated"));
        UUID uid = (UUID) auth.getPrincipal();
        Recipe r = recipeRepo.findById(id).orElseThrow();
        boolean isOwner = r.getChef().getId().equals(uid);
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isOwner && !isAdmin) return ResponseEntity.status(403).body(Map.of("error","forbidden"));
        r.setTitle(req.title != null ? req.title : r.getTitle());
        r.setSummary(req.summary != null ? req.summary : r.getSummary());
        if (req.ingredients != null) r.setIngredientsJson(mapper.writeValueAsString(req.ingredients));
        if (req.steps != null) r.setStepsJson(mapper.writeValueAsString(req.steps));
        if (req.labels != null) r.setLabels(req.labels);
        if (req.publish && r.getState() == Recipe.State.DRAFT) {
            r.setState(Recipe.State.PUBLISHED); r.setPublishedAt(Instant.now());
        }
        recipeRepo.save(r);
        return ResponseEntity.ok(Map.of("status","updated"));
    }

    @DeleteMapping("/chef/recipes/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id, Authentication auth) {
        if (auth==null) return ResponseEntity.status(401).body(Map.of("error","unauthenticated"));
        UUID uid = (UUID) auth.getPrincipal();
        Recipe r = recipeRepo.findById(id).orElseThrow();
        boolean isOwner = r.getChef().getId().equals(uid);
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isOwner && !isAdmin) return ResponseEntity.status(403).body(Map.of("error","forbidden"));
        recipeRepo.delete(r);
        return ResponseEntity.ok(Map.of("status","deleted"));
    }
}
