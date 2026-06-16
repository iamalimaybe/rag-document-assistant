$ErrorActionPreference = "Stop"

$baseUrl = "http://localhost:8080"
$samplePath = Join-Path (Get-Location) "samples\structure-aware-rag-policy.txt"

if (-not (Test-Path $samplePath)) {
    throw "Sample file not found: $samplePath. Run this script from the repository root."
}

Write-Host "`nThis script expects the backend to be running with:"
Write-Host "RAG_CHUNKING_STRATEGY=STRUCTURE_AWARE"
Write-Host "RAG_CHUNK_SIZE_CHARS=300"
Write-Host "RAG_CHUNK_OVERLAP_CHARS=50"
Write-Host ""
Write-Host "Use this in the backend terminal before starting the app:"
Write-Host '$env:RAG_CHUNKING_STRATEGY="STRUCTURE_AWARE"'
Write-Host '$env:RAG_CHUNK_SIZE_CHARS="300"'
Write-Host '$env:RAG_CHUNK_OVERLAP_CHARS="50"'
Write-Host ""

Write-Host "`nUploading structure-aware sample document..."
$uploadRaw = curl.exe -s -X POST "$baseUrl/api/documents" `
    -F "file=@$samplePath;type=text/plain"

$uploadedDocument = $uploadRaw | ConvertFrom-Json
$documentId = $uploadedDocument.id

if (-not $documentId) {
    Write-Host $uploadRaw
    throw "Upload did not return a document id."
}

Write-Host "Uploaded document id: $documentId"

Write-Host "`nFetching generated chunks..."
$chunks = Invoke-RestMethod `
    -Uri "$baseUrl/api/documents/$documentId/chunks" `
    -Method Get

$chunks | ConvertTo-Json -Depth 10

Write-Host "`nGenerating embeddings..."
$embeddingResult = Invoke-RestMethod `
    -Uri "$baseUrl/api/documents/$documentId/embeddings" `
    -Method Post

$embeddingResult | ConvertTo-Json -Depth 10

Write-Host "`nAsking structure-aware question..."
$bodyJson = @{
    question = "What should a structure-aware chunker prefer?"
    topK = 5
} | ConvertTo-Json -Compress

$response = Invoke-RestMethod `
    -Uri "$baseUrl/api/documents/$documentId/ask" `
    -Method Post `
    -ContentType "application/json" `
    -Body $bodyJson

$response | ConvertTo-Json -Depth 10

Write-Host "`nStructure-aware smoke flow completed."
Write-Host "`nAfter stopping the backend, clear the temporary environment variables:"
Write-Host "Remove-Item Env:RAG_CHUNKING_STRATEGY"
Write-Host "Remove-Item Env:RAG_CHUNK_SIZE_CHARS"
Write-Host "Remove-Item Env:RAG_CHUNK_OVERLAP_CHARS"