# ADR-015: pgvector for Embedding Storage

**Status:** Accepted  
**Date:** 2026-07-20  
**Deciders:** Engineering Team

---

## Context

The RAG pipeline requires a vector store for document embeddings. Options considered:

1. Dedicated vector database (Pinecone, Weaviate, Qdrant)
2. pgvector extension on existing PostgreSQL
3. OpenSearch with k-NN plugin

## Decision

Use **pgvector** as the primary vector store, co-located with the main PostgreSQL database.

## Rationale

- Eliminates a separate infrastructure dependency (no additional Docker service)
- Transactional consistency: embeddings stored in the same transaction as document metadata
- Spring AI provides `PgVectorStore` adapter out-of-the-box
- HNSW indexing supports approximate nearest-neighbor at scale
- Fallback: if Ollama is unavailable, text-search via PostgreSQL `tsvector` covers most queries

## Consequences

**Positive:**

- Single database for all persistence — simpler operations and backup
- No cross-service distributed transaction complexity

**Negative:**

- PostgreSQL becomes a combined OLTP + vector database — may need vertical scaling
- Maximum vector dimensions limited by PostgreSQL column size (~16K for `float4[]`)

## Schema

```sql
-- Vectors stored as float4 arrays with HNSW index
CREATE TABLE document_embeddings (
    id text PRIMARY KEY,
    content text,
    embedding vector(1536),
    metadata jsonb
);
CREATE INDEX ON document_embeddings USING hnsw (embedding vector_l2_ops);
```
