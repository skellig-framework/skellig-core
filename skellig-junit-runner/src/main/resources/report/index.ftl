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
    <#compress>
        <#list featuresReportDetails as frd>
            <div class="row">
                <div class="card">
                    <div class="card-header">
                        <h5 class="card-category">${featureTitle}</h5>
                        <h3 class="card-title">${frd.name}</h3>
                        <div class="test-step-progress">
                            <div class="test-step-progress-bar" role="progressbar"
                                 style="width: ${frd.totalPassedPercentage}%"
                                 aria-valuenow="${frd.totalPassedPercentage}" aria-valuemin="0" aria-valuemax="100">
                                ${frd.totalPassedTestSteps}/${frd.totalTestSteps}
                            </div>
                        </div>
                    </div>

                    <#list frd.testScenarioReportDetails as tsrd>
                        <#assign i = frd?index +"_"+tsrd?index />
                        <div id="featurePanel${i}" role="tablist" aria-multiselectable="true"
                             class="card-collapse">
                            <div class="card card-plain">
                                <div class="card-header" id="testScenarioHeader${i}">
                                    <a data-toggle="collapse" data-parent="#featurePanel${i}"
                                       href="#testStepsPanel${i}" aria-expanded="false"
                                       aria-controls="testStepsPanel${i}"
                                       class="collapsed <#if tsrd.passed>passed-color<#else>failed-color</#if>">
                                        ${tsrd.name}
                                        <i class="tim-icons icon-minimal-down"></i>
                                    </a>
                                </div>

                                <div id="testStepsPanel${i}" role="tablist" aria-multiselectable="true"
                                     class="collapse"
                                     aria-labelledby="testScenarioHeader${i}">
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
                                                        <i class="tim-icons icon-minimal-down"></i>
                                                    </a>
                                                </div>
                                                <div id="testStepPanel${si}" class="collapse" role="tabpanel"
                                                     aria-labelledby="testStepHeader${si}">
                                                    <#assign testStepDetails = step.properties?html?trim />
                                                    <#if testStepDetails?? && testStepDetails?has_content>
                                                        <div class="medium-text-panel">
                                                            ${propertiesTitle}
                                                        </div>
                                                        <div class="small-text-panel">
                                                            ${testStepDetails?replace("\n","<br>")}
                                                        </div>
                                                    </#if>
                                                    <#if step.testData?? && step.testData?trim?has_content>
                                                        <div class="medium-text-panel">
                                                            ${testDataTitle}
                                                        </div>
                                                        <div class="small-text-panel">
                                                            ${step.testData?html?trim?replace("\n","<br>")}
                                                        </div>
                                                    </#if>
                                                    <#if step.result?? && step.result?trim?has_content>
                                                        <div class="medium-text-panel">
                                                            ${responseTitle}
                                                        </div>
                                                        <div class="small-text-panel">
                                                            ${step.result?html?trim?replace("\n","<br>")}
                                                        </div>
                                                    </#if>
                                                    <#if step.validationDetails?? && step.validationDetails?trim?has_content>
                                                        <div class="medium-text-panel">
                                                            ${validationTitle}
                                                        </div>
                                                        <div class="small-text-panel">
                                                            ${step.validationDetails?html?trim?replace("\n","<br>")}
                                                        </div>
                                                    </#if>
                                                    <#if !step.passed>
                                                        <div class="medium-text-panel failed-content-color">
                                                            ${errorTitle}
                                                        </div>
                                                        <div class="small-text-panel failed-content-color">
                                                            ${step.errorLog?html?trim?replace("\n","<br>")}
                                                        </div>
                                                    </#if>
                                                    <#if step.attachments?? && step.attachments?has_content>
                                                        <#list step.attachments as attachment>
                                                            <#assign sai = si +"_"+attachment?index />
                                                            <div class="medium-text-panel">
                                                                <a data-toggle="collapse"
                                                                   data-parent="#testStepPanel${si}"
                                                                   href="#logPanel${sai}" aria-expanded="false"
                                                                   aria-controls="logPanel${sai}"
                                                                   class="collapsed passed-color">
                                                                    ${attachment.name}
                                                                    <i class="tim-icons icon-minimal-down"></i>
                                                                </a>
                                                            </div>
                                                            <div class="small-text-panel">
                                                                <div class="collapse" id="logPanel${sai}">
                                                                    <div class="card card-body">
                                                                        <#if attachment.class.simpleName == "LogAttachment">
                                                                            <#list attachment.data as log>
                                                                                <p>
                                                                                    ${log?html?trim?replace("\n","<br>")}
                                                                                </p>
                                                                            </#list>

                                                                        <#elseif attachment.class.simpleName == "LogRecordsFromFileAttachment">
                                                                            <p>
                                                                                ${attachment.data?html?trim?replace("\n","<br>")}
                                                                            </p>
                                                                        <#else>
                                                                        </#if>
                                                                    </div>
                                                                </div>
                                                            </div>
                                                        </#list>

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
        </#list>
    </#compress>
</body>
</html>