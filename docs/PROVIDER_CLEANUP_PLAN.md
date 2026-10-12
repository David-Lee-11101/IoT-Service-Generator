# Provider template boundary repair

Scope: SpecificGeneratorImpl database-derived labels and detail-info Java templates.
Preserve valid ASCII identifier labels and existing generated method names. Add tests
before repair for spaces, quotes/braces, Java keywords, non-string labels, and capitalization
collisions; rejected records must produce no generated files. Exercise actual FreeMarker
rendering for a valid record via a mocked mapper (no database or network).

Reject invalid or colliding derived Java identifiers before rendering. Render quoted
Java strings using an explicit escaped literal field; do not HTML-escape Java source.
Preserve historical single-quote metadata conversion as a documented legacy input
boundary; it cannot bypass identifier validation. No new dependencies or template logic.

Require filesystem atomic move support and propagate unsupported-move errors; remove
the ordinary-move fallback. Regression injects an unsupported atomic mover and proves
no destination and no temporary file remain. Historical provider adapter publishes
individual source files atomically; it is not a transactional whole-project publisher.
