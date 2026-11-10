package com.padmasiniAdmin.padmasiniAdmin_1.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.safety.Safelist;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.mongodb.ErrorCategory;
import com.mongodb.MongoWriteException;
import com.padmasiniAdmin.padmasiniAdmin_1.model.BlogModel;
import com.padmasiniAdmin.padmasiniAdmin_1.repository.BlogRepository;

@Service
public class BlogService {

    /** Error carrying the HTTP status the controller should return. */
    public static class BlogException extends RuntimeException {
        private final HttpStatus status;

        public BlogException(HttpStatus status, String message) {
            super(message);
            this.status = status;
        }

        public HttpStatus getStatus() {
            return status;
        }
    }

    private static final Pattern SLUG = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");
    private static final Pattern HTTP_URL = Pattern.compile("^https?://\\S+$", Pattern.CASE_INSENSITIVE);

    /** Allowed content: p, h2, h3, bold, italic, lists, links, images, blockquote, simple tables. No H1. */
    private static final Safelist SAFELIST = new Safelist()
            .addTags("p", "h2", "h3", "strong", "em", "ul", "ol", "li", "a", "img",
                    "blockquote", "table", "thead", "tbody", "tr", "th", "td", "br")
            .addAttributes("a", "href", "target")
            .addAttributes("img", "src", "alt")
            .addProtocols("a", "href", "http", "https", "mailto")
            .addProtocols("img", "src", "http", "https")
            .addEnforcedAttribute("a", "rel", "noopener noreferrer");

    private final BlogRepository repository;

    public BlogService(BlogRepository repository) {
        this.repository = repository;
    }

    /* ───────── queries ───────── */

    public List<Map<String, Object>> list() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Document d : repository.findAll()) {
            out.add(toResponse(d));
        }
        return out;
    }

    public Map<String, Object> get(String id) {
        return toResponse(load(parseId(id)));
    }

    /* ───────── commands ───────── */

    public Map<String, Object> create(BlogModel req) {
        validate(req);
        Date now = new Date();
        Date publishedAt = resolvePublishedAt(null, req.status(), req.publishedAt(), now);
        Document doc = buildDocument(req, publishedAt, now);
        try {
            ObjectId id = repository.insert(doc);
            doc.put("_id", id);
        } catch (MongoWriteException e) {
            throw duplicateOrRethrow(e);
        }
        return toResponse(doc);
    }

    public Map<String, Object> update(String id, BlogModel req) {
        ObjectId oid = parseId(id);
        Document existing = load(oid);
        validate(req);
        Date now = new Date();
        Date publishedAt = resolvePublishedAt(existing.getDate("publishedAt"), req.status(), req.publishedAt(), now);
        Document doc = buildDocument(req, publishedAt, now);
        try {
            repository.replace(oid, doc);
        } catch (MongoWriteException e) {
            throw duplicateOrRethrow(e);
        }
        doc.put("_id", oid);
        return toResponse(doc);
    }

    public Map<String, Object> setStatus(String id, String status) {
        if (!BlogModel.DRAFT.equals(status) && !BlogModel.PUBLISHED.equals(status)) {
            throw new BlogException(HttpStatus.BAD_REQUEST, "Status must be DRAFT or PUBLISHED.");
        }
        ObjectId oid = parseId(id);
        Document doc = load(oid);
        Date now = new Date();
        doc.put("status", status);
        doc.put("publishedAt", resolvePublishedAt(doc.getDate("publishedAt"), status, null, now));
        doc.put("updatedAt", now);
        repository.replace(oid, doc);
        return toResponse(doc);
    }

    public void delete(String id) {
        if (!repository.delete(parseId(id))) {
            throw new BlogException(HttpStatus.NOT_FOUND, "Post not found.");
        }
    }

    /* ───────── helpers ───────── */

    /**
     * publishedAt rules:
     *  - an explicit date from the admin (back-dating) wins;
     *  - otherwise an existing date is never overwritten;
     *  - otherwise it is set to now the first time the post becomes PUBLISHED.
     */
    private Date resolvePublishedAt(Date existing, String status, String requested, Date now) {
        if (requested != null && !requested.isBlank()) {
            try {
                return Date.from(Instant.parse(requested.trim()));
            } catch (Exception e) {
                throw new BlogException(HttpStatus.BAD_REQUEST, "publishedAt must be an ISO-8601 date.");
            }
        }
        if (existing != null) {
            return existing;
        }
        return BlogModel.PUBLISHED.equals(status) ? now : null;
    }

    private Document buildDocument(BlogModel r, Date publishedAt, Date updatedAt) {
        boolean hasImage = notBlank(r.image());
        List<String> keywords = new ArrayList<>();
        if (r.keywords() != null) {
            for (String k : r.keywords()) {
                if (notBlank(k) && keywords.stream().noneMatch(x -> x.equalsIgnoreCase(k.trim()))) {
                    keywords.add(k.trim());
                }
            }
        }
        return new Document()
                .append("category", r.category().trim())
                .append("title", r.title().trim())
                .append("slug", r.slug().trim())
                .append("shortDescription", r.shortDescription().trim())
                .append("image", hasImage ? r.image().trim() : "")
                .append("imageAlt", hasImage ? r.imageAlt().trim() : "")
                .append("content", sanitize(r.content()))
                .append("metaTitle", r.metaTitle() == null ? "" : r.metaTitle().trim())
                .append("metaDescription", r.metaDescription() == null ? "" : r.metaDescription().trim())
                .append("keywords", keywords)
                .append("status", r.status())
                .append("publishedAt", publishedAt)   // java.util.Date -> BSON date (or null for never published)
                .append("updatedAt", updatedAt);
    }

    /** Server-side clean-up. Never trust the browser. H1 becomes H2, scripts and event handlers are removed. */
    static String sanitize(String html) {
        if (html == null) {
            return "";
        }
        org.jsoup.nodes.Document parsed = Jsoup.parseBodyFragment(html);
        parsed.select("h1").tagName("h2");
        parsed.select("h4, h5, h6").tagName("h3");
        parsed.select("b").tagName("strong");
        parsed.select("i").tagName("em");
        parsed.select("div").tagName("p");
        return Jsoup.clean(parsed.body().html(), "", SAFELIST, new OutputSettings().prettyPrint(false));
    }

    private void validate(BlogModel r) {
        if (r == null) {
            throw new BlogException(HttpStatus.BAD_REQUEST, "Request body is required.");
        }
        List<String> errors = new ArrayList<>();
        if (!notBlank(r.category())) errors.add("category is required");
        if (!notBlank(r.title())) errors.add("title is required");
        if (!notBlank(r.slug())) errors.add("slug is required");
        else if (!SLUG.matcher(r.slug().trim()).matches())
            errors.add("slug may contain only lowercase letters, digits and single hyphens");
        if (!notBlank(r.shortDescription())) errors.add("shortDescription is required");
        if (notBlank(r.image())) {
            if (!HTTP_URL.matcher(r.image().trim()).matches()) errors.add("image must be an http(s) URL");
            if (!notBlank(r.imageAlt())) errors.add("imageAlt is required when image is set");
        }
        if (!BlogModel.DRAFT.equals(r.status()) && !BlogModel.PUBLISHED.equals(r.status()))
            errors.add("status must be DRAFT or PUBLISHED");
        if (!notBlank(sanitize(r.content()).replaceAll("<[^>]*>", "").trim())
                && (r.content() == null || !r.content().contains("<img")))
            errors.add("content is required");
        if (!errors.isEmpty()) {
            throw new BlogException(HttpStatus.BAD_REQUEST, String.join("; ", errors) + ".");
        }
    }

    private BlogException duplicateOrRethrow(MongoWriteException e) {
        if (e.getError().getCategory() == ErrorCategory.DUPLICATE_KEY) {
            return new BlogException(HttpStatus.CONFLICT, "A post with this slug already exists.");
        }
        throw e;
    }

    private ObjectId parseId(String id) {
        if (id == null || !ObjectId.isValid(id)) {
            throw new BlogException(HttpStatus.NOT_FOUND, "Post not found.");
        }
        return new ObjectId(id);
    }

    private Document load(ObjectId id) {
        return repository.findById(id)
                .orElseThrow(() -> new BlogException(HttpStatus.NOT_FOUND, "Post not found."));
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    /** _id as a hex string and dates as ISO-8601 strings, ready for JSON. */
    private Map<String, Object> toResponse(Document d) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : d.entrySet()) {
            Object v = e.getValue();
            if (v instanceof ObjectId oid) v = oid.toHexString();
            else if (v instanceof Date date) v = date.toInstant().toString();
            out.put(e.getKey(), v);
        }
        return out;
    }
}