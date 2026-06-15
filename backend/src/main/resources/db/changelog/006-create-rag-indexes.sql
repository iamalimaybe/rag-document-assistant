CREATE INDEX idx_document_chunks_document_id
    ON document_chunks (document_id);

CREATE INDEX idx_question_answer_runs_document_id
    ON question_answer_runs (document_id);

CREATE INDEX idx_retrieved_chunks_qa_run_id
    ON retrieved_chunks (qa_run_id);

CREATE INDEX idx_retrieved_chunks_document_chunk_id
    ON retrieved_chunks (document_chunk_id);