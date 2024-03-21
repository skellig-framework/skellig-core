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
    <div class="row">
        <div class="card">
            <div class="card-header">
                <h5 class="card-category">${featureTitle}</h5>
                <h3 class="card-title">
                    ${feature.name}
                    <div class="duration">
                        ${feature.totalDuration}
                    </div>
                </h3>
                <div class="test-step-progress">
                    <div class="test-step-progress-bar" role="progressbar"
                         style="width: ${feature.totalPassedPercentage}%"
                         aria-valuenow="${feature.totalPassedPercentage}" aria-valuemin="0" aria-valuemax="100">
                        ${feature.totalPassedTestSteps}/${feature.totalTestSteps}
                    </div>
                </div>
                <#if feature.tagsLine?? && feature.tagsLine?trim?has_content>
                    <div class="tags-text-panel">
                        ${feature.tagsLine}
                    </div>
                </#if>

                <#if feature.hooksReportDetails?? && feature.hooksReportDetails?has_content>
                    <div id="hooksParentPanel" role="tablist" aria-multiselectable="true"
                         class="card-collapse">
                        <div class="card card-plain">
                            <div class="card-header" id="hooksHeader">
                                <a data-toggle="collapse"
                                   href="#hooksPanel" aria-expanded="false"
                                   aria-controls="testStepsPanel" data-parent="#hooksParentPanel"
                                   class="collapsed <#if feature.passed>passed-color<#else>failed-color</#if>">
                                    ${hooksTitle}
                                    <div class="duration">
                                        ${feature.hooksDurationFormatted}
                                    </div>
                                </a>
                            </div>

                            <div id="hooksPanel" role="tablist" aria-multiselectable="true"
                                 class="collapse" aria-labelledby="hooksHeader">
                                <div class="card-body">
                                    <#list feature.hooksReportDetails as hook>
                                        <#assign i = hook?index />
                                        <div class="card card-plain">
                                            <div class="card-header" id="hookHeader${i}">
                                                <a data-toggle="collapse" data-parent="#hookPanel${i}"
                                                   href="#hookPanel${i}" aria-expanded="false"
                                                   aria-controls="hookPanel${i}"
                                                   class="collapsed <#if hook.passed>passed-color<#else>failed-color</#if>">
                                                    ${hook.methodName}
                                                    <div class="duration">
                                                        ${hook.durationFormatted}
                                                    </div>
                                                </a>
                                            </div>
                                            <div id="hookPanel${i}" class="collapse" role="tabpanel"
                                                 aria-labelledby="hookHeader${i}">
                                                <#if !hook.passed>
                                                    <div class="medium-text-panel failed-content-color">
                                                        ${errorTitle}
                                                    </div>
                                                    <pre class="small-text-panel failed-content-color">${hook.errorLog?html?trim}</pre>
                                                </#if>
                                                <#if hook.logRecords?? && hook.logRecords?has_content>
                                                    <div class="card card-body">
                                                    <pre class="small-text-panel">
                                                        <#list hook.logRecords as log>
                                                            <p>${log?html?trim}</p>
                                                        </#list>
                                                    </pre>
                                                    </div>
                                                </#if>
                                            </div>
                                        </div>
                                    </#list>

                                </div>

                            </div>
                        </div>

                    </div>
                </#if>
            </div>

            <#list feature.testScenarioReportDetails as tsrd>
                <#assign i = tsrd?index />
                <div id="featurePanel${i}" role="tablist" aria-multiselectable="true"
                     class="card-collapse">
                    <div class="card card-plain">
                        <div class="card-header" id="testScenarioHeader${i}">
                            <a data-toggle="collapse" data-parent="#featurePanel${i}"
                               href="#testStepsPanel${i}" aria-expanded="false"
                               aria-controls="testStepsPanel${i}"
                               class="collapsed <#if tsrd.passed>passed-color<#else>failed-color</#if>">
                                ${tsrd.name}
                                <div class="duration">
                                    ${tsrd.scenarioDurationFormatted}
                                </div>
                            </a>
                        </div>

                        <div id="testStepsPanel${i}" role="tablist" aria-multiselectable="true"
                             class="collapse"
                             aria-labelledby="testScenarioHeader${i}">
                            <#if tsrd.tagsLine?? && tsrd.tagsLine?trim?has_content>
                                <div class="card-body">
                                    <div class="tags-text-panel">
                                        ${tsrd.tagsLine}
                                    </div>
                                </div>
                            </#if>
                            <div class="card-body">
                                <#list tsrd.testStepReportDetails as step>
                                    <#assign si = i +"_"+step?index />
                                    <div class="card card-plain">
                                        <div class="card-header" id="testStepHeader${si}">
                                            <a data-toggle="collapse" data-parent="#testStepsPanel${i}"
                                               href="#testStepPanel${si}" aria-expanded="false"
                                               aria-controls="testStepsPanel${si}"
                                               class="collapsed <#if step.ignored>ignored-color<#elseif step.passed>passed-color<#else>failed-color</#if>">
                                                ${step.name}
                                                <div class="duration">
                                                    ${step.durationFormatted}
                                                </div>
                                            </a>
                                        </div>
                                        <div id="testStepPanel${si}" class="collapse" role="tabpanel"
                                             aria-labelledby="testStepHeader${si}">
                                            <#assign parameters = step.parameters?html?trim />
                                            <#if parameters?? && parameters?has_content>
                                                <div class="medium-text-panel">
                                                    ${parametersTitle}
                                                </div>
                                                <pre class="small-text-panel">${parameters?html?trim}</pre>
                                            </#if>
                                            <#assign testStepDetails = step.properties?html?trim />
                                            <#if testStepDetails?? && testStepDetails?has_content>
                                                <div class="medium-text-panel">
                                                    ${propertiesTitle}
                                                </div>
                                                <pre class="small-text-panel">${testStepDetails?html?trim}</pre>
                                            </#if>
                                            <#if step.testData?? && step.testData?trim?has_content>
                                                <div class="medium-text-panel">
                                                    ${testDataTitle}
                                                </div>
                                                <pre class="small-text-panel">${step.testData?html?trim}</pre>
                                            </#if>
                                            <#if step.result?? && step.result?trim?has_content>
                                                <div class="medium-text-panel">
                                                    ${responseTitle}
                                                </div>
                                                <pre class="small-text-panel">${step.result?html?trim}</pre>
                                            </#if>
                                            <#if step.validationDetails?? && step.validationDetails?trim?has_content>
                                                <div class="medium-text-panel">
                                                    ${validationTitle?html?trim}
                                                </div>
                                                <pre class="small-text-panel">${step.validationDetails?html?trim}</pre>
                                            </#if>
                                            <#if !step.passed>
                                                <div class="medium-text-panel failed-content-color">
                                                    ${errorTitle}
                                                </div>
                                                <pre class="small-text-panel failed-content-color">${step.errorLog?html?trim}</pre>
                                            </#if>
                                            <#if step.logRecords?? && step.logRecords?has_content>
                                                <div class="medium-text-panel">
                                                    <a data-toggle="collapse" data-parent="#testStepPanel${si}"
                                                       href="#logPanel${si}" aria-expanded="false"
                                                       aria-controls="logPanel${si}"
                                                       class="collapsed passed-color">
                                                        ${logTitle}
                                                    </a>
                                                </div>
                                                <div class="small-text-panel">
                                                    <div class="collapse" id="logPanel${si}">
                                                        <div class="card card-body">
                                                                <pre class="small-text-panel">
                                                                    <#list step.logRecords as log>
                                                                        <p>${log?html?trim}</p>
                                                                    </#list>
                                                                </pre>
                                                        </div>
                                                    </div>
                                                </div>
                                            </#if>
                                        </div>
                                    </div>
                                </#list>
                            </div>

                        </div>
                    </div>

                </div>
            </#list>
        </div>
    </div>
</body>
</html>