import { type SyntheticEvent, useEffect, useMemo, useState } from 'react'
import './App.css'

type DocumentStatus = 'UPLOADED' | 'PROCESSING' | 'READY' | 'FAILED'
type SourceType = 'PDF' | 'TXT' | 'MD'
type AnswerStatus = 'ANSWERED' | 'INSUFFICIENT_CONTEXT' | 'FAILED'

type DocumentResponse = {
    id: number
    originalFilename: string
    contentType: string
    status: DocumentStatus
    sourceType: SourceType
    extractedTextLength: number
    failureReason?: string | null
    createdAt: string
    updatedAt: string
}

type EmbeddedDocumentResponse = {
    documentId: number
    embeddedChunkCount: number
}

type RetrievedChunkResponse = {
    chunkId: number
    documentId: number
    chunkIndex: number
    pageNumber?: number | null
    similarityScore: number
    tokenEstimate: number
    content: string
}

type AskDocumentResponse = {
    qaRunId: number
    documentId: number
    question: string
    answer: string
    answerStatus: AnswerStatus
    failureReason?: string | null
    modelName: string
    embeddingModelName: string
    topK: number
    citations: RetrievedChunkResponse[]
    retrievedChunks: RetrievedChunkResponse[]
}

type QuestionAnswerRunSummaryResponse = {
    id: number
    documentId: number
    question: string
    answerStatus: AnswerStatus
    modelName: string
    embeddingModelName: string
    topK: number
    createdAt: string
    updatedAt: string
}

type RetrievedChunkSnapshotResponse = {
    id: number
    qaRunId: number
    documentChunkId: number
    rank: number
    similarityScore: number
    contentSnapshot: string
    pageNumberSnapshot?: number | null
    createdAt: string
}

type QuestionAnswerRunDetailResponse = {
    id: number
    documentId: number
    question: string
    answer: string
    answerStatus: AnswerStatus
    failureReason?: string | null
    modelName: string
    embeddingModelName: string
    topK: number
    rawPrompt: string
    rawModelOutput: string
    retrievedChunks: RetrievedChunkSnapshotResponse[]
    createdAt: string
    updatedAt: string
}

async function apiRequest<T>(path: string, options?: RequestInit): Promise<T> {
    const response = await fetch(path, options)

    if (!response.ok) {
        const responseText = await response.text()
        throw new Error(responseText || `Request failed with status ${response.status}`)
    }

    return response.json() as Promise<T>
}

function formatDateTime(value: string): string {
    return new Date(value).toLocaleString()
}

function formatScore(value: number | undefined): string {
    if (value === undefined || value === null || Number.isNaN(value)) {
        return 'N/A'
    }

    return value.toFixed(4)
}

function statusLabel(status: AnswerStatus | DocumentStatus): string {
    return status.replaceAll('_', ' ')
}

function App() {
    const [documents, setDocuments] = useState<DocumentResponse[]>([])
    const [selectedDocumentId, setSelectedDocumentId] = useState<number | null>(null)
    const [selectedFile, setSelectedFile] = useState<File | null>(null)

    const [question, setQuestion] = useState('What is this document about?')
    const [topK, setTopK] = useState(5)

    const [answer, setAnswer] = useState<AskDocumentResponse | null>(null)
    const [history, setHistory] = useState<QuestionAnswerRunSummaryResponse[]>([])
    const [runDetail, setRunDetail] = useState<QuestionAnswerRunDetailResponse | null>(null)

    const [loadingDocuments, setLoadingDocuments] = useState(false)
    const [uploading, setUploading] = useState(false)
    const [embedding, setEmbedding] = useState(false)
    const [asking, setAsking] = useState(false)
    const [loadingHistory, setLoadingHistory] = useState(false)
    const [loadingRunDetailId, setLoadingRunDetailId] = useState<number | null>(null)

    const [message, setMessage] = useState<string | null>(null)
    const [error, setError] = useState<string | null>(null)

    const selectedDocument = useMemo(() => {
        return documents.find((document) => document.id === selectedDocumentId) ?? null
    }, [documents, selectedDocumentId])

    async function loadDocuments() {
        setLoadingDocuments(true)
        setError(null)

        try {
            const response = await apiRequest<DocumentResponse[]>('/api/documents')
            setDocuments(response)

            if (selectedDocumentId === null && response.length > 0) {
                setSelectedDocumentId(response[0].id)
            }
        } catch (requestError) {
            setError(requestError instanceof Error ? requestError.message : 'Failed to load documents.')
        } finally {
            setLoadingDocuments(false)
        }
    }

    async function loadHistory(documentId: number) {
        setLoadingHistory(true)
        setError(null)

        try {
            const response = await apiRequest<QuestionAnswerRunSummaryResponse[]>(
                `/api/documents/${documentId}/qa-runs`,
            )
            setHistory(response)
        } catch (requestError) {
            setError(requestError instanceof Error ? requestError.message : 'Failed to load QA history.')
        } finally {
            setLoadingHistory(false)
        }
    }

    useEffect(() => {
        loadDocuments()
    }, [])

    useEffect(() => {
        if (selectedDocumentId !== null) {
            loadHistory(selectedDocumentId)
            setAnswer(null)
            setRunDetail(null)
        }
    }, [selectedDocumentId])

    async function handleUpload(event: SyntheticEvent<HTMLFormElement>) {
        event.preventDefault()

        if (!selectedFile) {
            setError('Choose a TXT, MD, or PDF file first.')
            return
        }

        setUploading(true)
        setError(null)
        setMessage(null)

        try {
            const formData = new FormData()
            formData.append('file', selectedFile)

            const uploadedDocument = await apiRequest<DocumentResponse>('/api/documents', {
                method: 'POST',
                body: formData,
            })

            setSelectedFile(null)
            setSelectedDocumentId(uploadedDocument.id)
            setMessage(`Uploaded ${uploadedDocument.originalFilename}.`)
            await loadDocuments()
            await loadHistory(uploadedDocument.id)
        } catch (requestError) {
            setError(requestError instanceof Error ? requestError.message : 'Upload failed.')
        } finally {
            setUploading(false)
        }
    }

    async function handleGenerateEmbeddings() {
        if (selectedDocumentId === null) {
            setError('Select a document first.')
            return
        }

        setEmbedding(true)
        setError(null)
        setMessage(null)

        try {
            const response = await apiRequest<EmbeddedDocumentResponse>(
                `/api/documents/${selectedDocumentId}/embeddings`,
                {
                    method: 'POST',
                },
            )

            setMessage(`Generated embeddings for ${response.embeddedChunkCount} chunks.`)
            await loadDocuments()
        } catch (requestError) {
            setError(requestError instanceof Error ? requestError.message : 'Embedding generation failed.')
        } finally {
            setEmbedding(false)
        }
    }

    async function handleAsk(event: SyntheticEvent<HTMLFormElement>) {
        event.preventDefault()

        if (selectedDocumentId === null) {
            setError('Select a document first.')
            return
        }

        if (!question.trim()) {
            setError('Question is required.')
            return
        }

        setAsking(true)
        setError(null)
        setMessage(null)
        setRunDetail(null)

        try {
            const response = await apiRequest<AskDocumentResponse>(
                `/api/documents/${selectedDocumentId}/ask`,
                {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        question: question.trim(),
                        topK,
                    }),
                },
            )

            setAnswer(response)
            setMessage(`Created QA run ${response.qaRunId}.`)
            await loadHistory(selectedDocumentId)
        } catch (requestError) {
            setError(requestError instanceof Error ? requestError.message : 'Question answering failed.')
        } finally {
            setAsking(false)
        }
    }

    async function handleLoadRunDetail(runId: number) {
        setLoadingRunDetailId(runId)
        setError(null)

        try {
            const response = await apiRequest<QuestionAnswerRunDetailResponse>(`/api/qa-runs/${runId}`)
            setRunDetail(response)
        } catch (requestError) {
            setError(requestError instanceof Error ? requestError.message : 'Failed to load QA run detail.')
        } finally {
            setLoadingRunDetailId(null)
        }
    }

    return (
        <main className="app-shell">
            <header className="hero">
                <div>
                    <p className="eyebrow">RAG Document Assistant</p>
                    <h1>Grounded document QA with citations and audit history</h1>
                    <p className="hero-copy">
                        Upload a document, generate embeddings, ask grounded questions, and inspect the
                        evidence used by each answer.
                    </p>
                </div>
            </header>

            {error && <div className="alert alert-error">{error}</div>}
            {message && <div className="alert alert-success">{message}</div>}

            <section className="grid two-column">
                <div className="card">
                    <div className="card-header">
                        <div>
                            <h2>Upload document</h2>
                            <p>Supported by the backend: TXT, MD, and PDF without OCR.</p>
                        </div>
                    </div>

                    <form onSubmit={handleUpload} className="stack">
                        <input
                            type="file"
                            accept=".txt,.md,.pdf,text/plain,text/markdown,application/pdf"
                            onChange={(event) => setSelectedFile(event.target.files?.[0] ?? null)}
                        />

                        <button type="submit" disabled={uploading}>
                            {uploading ? 'Uploading...' : 'Upload'}
                        </button>
                    </form>
                </div>

                <div className="card">
                    <div className="card-header">
                        <div>
                            <h2>Selected document</h2>
                            <p>Choose a document before embedding or asking questions.</p>
                        </div>
                        <button type="button" className="secondary" onClick={loadDocuments}>
                            {loadingDocuments ? 'Loading...' : 'Refresh'}
                        </button>
                    </div>

                    {documents.length === 0 ? (
                        <p className="muted">No documents found yet.</p>
                    ) : (
                        <div className="stack">
                            <select
                                value={selectedDocumentId ?? ''}
                                onChange={(event) => setSelectedDocumentId(Number(event.target.value))}
                            >
                                {documents.map((document) => (
                                    <option key={document.id} value={document.id}>
                                        #{document.id} {document.originalFilename}
                                    </option>
                                ))}
                            </select>

                            {selectedDocument && (
                                <div className="meta-box">
                                    <div>
                                        <span>Status</span>
                                        <strong>{statusLabel(selectedDocument.status)}</strong>
                                    </div>
                                    <div>
                                        <span>Type</span>
                                        <strong>{selectedDocument.sourceType}</strong>
                                    </div>
                                    <div>
                                        <span>Extracted text length</span>
                                        <strong>{selectedDocument.extractedTextLength}</strong>
                                    </div>
                                    <div>
                                        <span>Uploaded</span>
                                        <strong>{formatDateTime(selectedDocument.createdAt)}</strong>
                                    </div>
                                </div>
                            )}

                            <button type="button" onClick={handleGenerateEmbeddings} disabled={embedding}>
                                {embedding ? 'Generating embeddings...' : 'Generate embeddings'}
                            </button>
                        </div>
                    )}
                </div>
            </section>

            <section className="card">
                <div className="card-header">
                    <div>
                        <h2>Ask a grounded question</h2>
                        <p>
                            The backend retrieves candidate chunks, selects prompt context, asks the local LLM,
                            validates citations, and stores the QA run.
                        </p>
                    </div>
                </div>

                <form onSubmit={handleAsk} className="question-form">
          <textarea
              value={question}
              onChange={(event) => setQuestion(event.target.value)}
              rows={4}
              placeholder="Ask a question about the selected document"
          />

                    <div className="inline-controls">
                        <label>
                            Top K
                            <input
                                type="number"
                                min={1}
                                max={20}
                                value={topK}
                                onChange={(event) => setTopK(Number(event.target.value))}
                            />
                        </label>

                        <button type="submit" disabled={asking || selectedDocumentId === null}>
                            {asking ? 'Asking...' : 'Ask document'}
                        </button>
                    </div>
                </form>
            </section>

            {answer && (
                <section className="card">
                    <div className="card-header">
                        <div>
                            <h2>Answer</h2>
                            <p>
                                Run #{answer.qaRunId} using {answer.modelName} and {answer.embeddingModelName}
                            </p>
                        </div>
                        <span className={`status-pill ${answer.answerStatus.toLowerCase()}`}>
              {statusLabel(answer.answerStatus)}
            </span>
                    </div>

                    {answer.failureReason && <div className="alert alert-error">{answer.failureReason}</div>}

                    <p className="answer-text">{answer.answer}</p>

                    <div className="grid two-column">
                        <EvidencePanel
                            title="Citations"
                            description="Chunks the model cited as supporting the answer."
                            chunks={answer.citations}
                            emptyText="No citations. This is expected for insufficient context."
                        />

                        <EvidencePanel
                            title="Retrieved chunks"
                            description="Candidate chunks returned by vector search."
                            chunks={answer.retrievedChunks}
                            emptyText="No retrieved chunks returned."
                        />
                    </div>
                </section>
            )}

            <section className="card">
                <div className="card-header">
                    <div>
                        <h2>QA history</h2>
                        <p>Stored QA runs for the selected document.</p>
                    </div>
                    {selectedDocumentId !== null && (
                        <button type="button" className="secondary" onClick={() => loadHistory(selectedDocumentId)}>
                            {loadingHistory ? 'Loading...' : 'Refresh history'}
                        </button>
                    )}
                </div>

                {history.length === 0 ? (
                    <p className="muted">No QA runs found for this document yet.</p>
                ) : (
                    <div className="history-list">
                        {history.map((run) => (
                            <article key={run.id} className="history-item">
                                <div>
                                    <div className="history-title">#{run.id} {run.question}</div>
                                    <div className="history-meta">
                                        {statusLabel(run.answerStatus)} · topK {run.topK} · {formatDateTime(run.createdAt)}
                                    </div>
                                </div>

                                <button
                                    type="button"
                                    className="secondary"
                                    onClick={() => handleLoadRunDetail(run.id)}
                                    disabled={loadingRunDetailId === run.id}
                                >
                                    {loadingRunDetailId === run.id ? 'Loading...' : 'View details'}
                                </button>
                            </article>
                        ))}
                    </div>
                )}
            </section>

            {runDetail && (
                <section className="card">
                    <div className="card-header">
                        <div>
                            <h2>QA run detail</h2>
                            <p>Run #{runDetail.id} audit data.</p>
                        </div>
                        <span className={`status-pill ${runDetail.answerStatus.toLowerCase()}`}>
              {statusLabel(runDetail.answerStatus)}
            </span>
                    </div>

                    <div className="stack">
                        <div>
                            <h3>Question</h3>
                            <p>{runDetail.question}</p>
                        </div>

                        <div>
                            <h3>Answer</h3>
                            <p>{runDetail.answer}</p>
                        </div>

                        <SnapshotPanel snapshots={runDetail.retrievedChunks} />

                        <details>
                            <summary>Raw prompt</summary>
                            <pre>{runDetail.rawPrompt}</pre>
                        </details>

                        <details>
                            <summary>Raw model output</summary>
                            <pre>{runDetail.rawModelOutput}</pre>
                        </details>
                    </div>
                </section>
            )}
        </main>
    )
}

function EvidencePanel({
                           title,
                           description,
                           chunks,
                           emptyText,
                       }: {
    title: string
    description: string
    chunks: RetrievedChunkResponse[]
    emptyText: string
}) {
    return (
        <div className="evidence-panel">
            <h3>{title}</h3>
            <p className="muted">{description}</p>

            {chunks.length === 0 ? (
                <p className="muted">{emptyText}</p>
            ) : (
                <div className="chunk-list">
                    {chunks.map((chunk) => (
                        <article key={`${title}-${chunk.chunkId}`} className="chunk-card">
                            <div className="chunk-meta">
                                <span>Chunk #{chunk.chunkIndex}</span>
                                <span>Score {formatScore(chunk.similarityScore)}</span>
                                <span>Tokens {chunk.tokenEstimate}</span>
                            </div>
                            <p>{chunk.content}</p>
                        </article>
                    ))}
                </div>
            )}
        </div>
    )
}

function SnapshotPanel({ snapshots }: { snapshots: RetrievedChunkSnapshotResponse[] }) {
    return (
        <div>
            <h3>Retrieved chunk snapshots</h3>

            {snapshots.length === 0 ? (
                <p className="muted">No chunk snapshots stored.</p>
            ) : (
                <div className="chunk-list">
                    {snapshots.map((snapshot) => (
                        <article key={snapshot.id} className="chunk-card">
                            <div className="chunk-meta">
                                <span>Rank {snapshot.rank}</span>
                                <span>Chunk ID {snapshot.documentChunkId}</span>
                                <span>Score {formatScore(snapshot.similarityScore)}</span>
                            </div>
                            <p>{snapshot.contentSnapshot}</p>
                        </article>
                    ))}
                </div>
            )}
        </div>
    )
}

export default App