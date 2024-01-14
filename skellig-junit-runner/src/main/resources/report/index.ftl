<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">

    <script src="js/jquery.min.js"></script>
    <script src="js/bootstrap.min.js"></script>
    <link rel="stylesheet" href="css/bootstrap.min.css"/>
    <link rel="stylesheet" href="css/skellig.css"/>

    <title>Skellig Test Report</title>
</head>

<body class="dark-background">

<div class="container-md">
    <div class="row">
        <h3 class="card-title">Skellig Test Report</h3>
    </div>
    <#list featuresReportDetails as frd>
        <div class="row">
            <div class="card">
                <div class="card-header">
                    <h5 class="card-category">${featureTitle}</h5>
                    <a href="feature-reports/${frd.name}.html"
                       <#if frd.passed>passed-color<#else>failed-color</#if>>
                        <h3 class="card-title">${frd.name}
                            <div class="duration">
                                ${frd.totalDuration}
                            </div>
                        </h3>
                    </a>
                    <div class="test-step-progress">
                        <div class="test-step-progress-bar" role="progressbar"
                             style="width: ${frd.totalPassedPercentage}%"
                             aria-valuenow="${frd.totalPassedPercentage}" aria-valuemin="0" aria-valuemax="100">
                            ${frd.totalPassedTestSteps}/${frd.totalTestSteps}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </#list>
</body>
</html>