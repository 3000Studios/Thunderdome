# Codex Bridge Monitor Script for Thunder Dome
# Run this script in PowerShell or from Codex terminal to see real-time updates of Antigravity's actions!

Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host "   THUNDER DOME (AEROSTRIKE) AGENT BRIDGE MONITOR       " -ForegroundColor Yellow
Write-Host "=========================================================" -ForegroundColor Cyan

$workspacePath = "c:\Users\MrJws\OneDrive\Workspaces\Thunder Dome"
$transcriptLog = "C:\Users\MrJws\.gemini\antigravity\brain\15baa7c6-3701-4d45-8501-acc79ffd3170\.system_generated\logs\transcript.jsonl"

Write-Host "`n[1] LATEST GIT COMMITS:" -ForegroundColor Green
Set-Location $workspacePath
git log -n 5 --oneline

Write-Host "`n[2] PROJECT FILE STATUS:" -ForegroundColor Green
git status -s

Write-Host "`n[3] RECENT AGENT LOGS (TRANSCRIPT SNAPSHOT):" -ForegroundColor Green
if (Test-Path $transcriptLog) {
    Get-Content $transcriptLog -Tail 15 | ForEach-Object {
        try {
            $json = $_ | ConvertFrom-Json
            Write-Host "-> [$($json.created_at)] $($json.type): $($json.tool_calls[0].toolSummary)" -ForegroundColor Gray
        } catch {
            Write-Host "-> $_" -ForegroundColor Gray
        }
    }
} else {
    Write-Host "Transcript log not found at standard path." -ForegroundColor Red
}

Write-Host "`n=========================================================" -ForegroundColor Cyan
Write-Host "  BRIDGE ACTIVE - SCRIPT READY FOR CODEX CONTINUOUS RUN  " -ForegroundColor Green
Write-Host "=========================================================" -ForegroundColor Cyan
