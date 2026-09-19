param([string[]]$Names = @('single', 'merged-long'))
$ErrorActionPreference = 'Stop'
$sampleRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../target/work-record-export-samples'))
Write-Output 'Starting hidden Word instance'
$word = New-Object -ComObject Word.Application
$word.Visible = $false
$word.DisplayAlerts = 0
$results = @()
try {
    foreach ($name in $Names) {
        $document = $null
        try {
            $source = Join-Path $sampleRoot ($name + '.docx')
            Write-Output "Opening $name"
            $document = $word.Documents.Open($source, $false, $true, $false)
            Write-Output "Paginating $name"
            $document.Repaginate()
            Write-Output ("Pages: " + $document.ComputeStatistics(2))
            $tables = @()
            foreach ($table in $document.Tables) {
                $start = $table.Range.Duplicate
                $start.Collapse(1)
                $tables += [PSCustomObject]@{ rows = $table.Rows.Count; startPage = $start.Information(3); endPage = $table.Range.Information(3); repeatHeader = $table.Rows.Item(1).HeadingFormat }
                [void][Runtime.InteropServices.Marshal]::FinalReleaseComObject($start)
                [void][Runtime.InteropServices.Marshal]::FinalReleaseComObject($table)
            }
            $covers = @()
            foreach ($paragraph in $document.Paragraphs) {
                if ($paragraph.Range.Font.Size -eq 22 -and $paragraph.Range.Text -like '*2026*') {
                    $covers += $paragraph.Range.Information(3)
                }
                [void][Runtime.InteropServices.Marshal]::FinalReleaseComObject($paragraph)
            }
            if ($tables.Count -ne $(if ($name -eq 'single') { 2 } else { 6 })) { throw 'Unexpected table count' }
            if (@($tables | Where-Object { $_.repeatHeader -ne -1 }).Count -gt 0) { throw 'Missing repeating header' }
            if ($tables[0].endPage -le $tables[0].startPage) { throw 'Long table did not span pages' }
            if ($covers.Count -ne $(if ($name -eq 'single') { 1 } else { 3 })) { throw 'Missing record cover' }
            if (@($covers | Select-Object -Unique).Count -ne $covers.Count) { throw 'Records share a cover page' }
            $results += [PSCustomObject]@{ file = $name; pages = $document.ComputeStatistics(2); tables = $tables; coverPages = $covers }
        } finally {
            if ($null -ne $document) { $document.Close(0); [void][Runtime.InteropServices.Marshal]::FinalReleaseComObject($document) }
        }
    }
    $results | ConvertTo-Json -Depth 6 | Set-Content -Encoding UTF8 (Join-Path $sampleRoot 'word-layout.json')
    $results | Format-Table
} finally {
    $word.Quit()
    [void][Runtime.InteropServices.Marshal]::FinalReleaseComObject($word)
}
