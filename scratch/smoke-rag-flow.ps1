$ErrorActionPreference = "Stop"

$baseUrl = "http://localhost:8080"
$samplePath = Join-Path (Get-Location) "samples\rag-sample-policy.txt"

if (-not (Test-Path $samplePath)) {
    throw "Sample file not found: $samplePath. Run this script from the repository root."
}

Write-Host "`nUploading sample document..."
$uploadRaw = curl.exe -s -X POST "$baseUrl/api/documents" `
    -F "file=@$samplePath;type=text/plain"

$uploadedDocument = $uploadRaw | ConvertFrom-Json
$documentId = $uploadedDocument.id

if (-not $documentId) {
    Write-Host $uploadRaw
    throw "Upload did not return a document id."
}

Write-Host "Uploaded document id: $documentId"

Write-Host "`nGenerating embeddings..."
$embeddingResult = Invoke-RestMethod `
    -Uri "$baseUrl/api/documents/$documentId/embeddings" `
    -Method Post

$embeddingResult | ConvertTo-Json -Depth 10

Write-Host "`nAsking answerable question..."
$answerableBody = @{
    question = "What should RAG systems use to answer questions?"
    topK = 5
} | ConvertTo-Json -Compress

$answerableResponse = Invoke-RestMethod `
    -Uri "$baseUrl/api/documents/$documentId/ask" `
    -Method Post `
    -ContentType "application/json" `
    -Body $answerableBody

$answerableResponse | ConvertTo-Json -Depth 10

Write-Host "`nFetching QA run detail for answerable question..."
$answerableRunDetail = Invoke-RestMethod `
    -Uri "$baseUrl/api/qa-runs/$($answerableResponse.qaRunId)" `
    -Method Get

$answerableRunDetail | ConvertTo-Json -Depth 10

Write-Host "`nAsking missing-info question..."
$missingInfoBody = @{
    question = "What is the refund period?"
    topK = 5
} | ConvertTo-Json -Compress

$missingInfoResponse = Invoke-RestMethod `
    -Uri "$baseUrl/api/documents/$documentId/ask" `
    -Method Post `
    -ContentType "application/json" `
    -Body $missingInfoBody

$missingInfoResponse | ConvertTo-Json -Depth 10

Write-Host "`nFetching document QA run history..."
$qaHistory = Invoke-RestMethod `
    -Uri "$baseUrl/api/documents/$documentId/qa-runs" `
    -Method Get

$qaHistory | ConvertTo-Json -Depth 10

Write-Host "`nSmoke flow completed."