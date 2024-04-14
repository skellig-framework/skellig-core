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
            </div>

            <#if feature.beforeHooksReportDetails?? && feature.beforeHooksReportDetails?has_content>
                <div id="beforeHooksParentPanel" role="tablist" aria-multiselectable="true"
                     class="card-collapse">
                    <div class="card card-plain">
                        <div class="card-header" id="beforeHooksHeader">
                            <a data-toggle="collapse"
                               href="#beforeHooksPanel" aria-expanded="false"
                               aria-controls="beforeHooksPanel" data-parent="#beforeHooksParentPanel"
                               class="collapsed <#if feature.passed>passed-color<#else>failed-color</#if>">
                                ${hooksTitle}
                                <div class="duration">
                                    ${feature.beforeHooksDurationFormatted}
                                </div>
                            </a>
                        </div>

                        <div id="beforeHooksPanel" role="tablist" aria-multiselectable="true"
                             class="collapse" aria-labelledby="beforeHooksHeader">
                            <div class="card-body">
                                <#list feature.beforeHooksReportDetails as hook>
                                    <#assign i = hook?index />
                                    <div class="card card-plain">
                                        <div class="card-header" id="beforeHookHeader${i}">
                                            <a data-toggle="collapse" data-parent="#beforeHookPanel${i}"
                                               href="#beforeHookPanel${i}" aria-expanded="false"
                                               aria-controls="beforeHookPanel${i}"
                                               class="collapsed <#if hook.passed>passed-color<#else>failed-color</#if>">
                                                ${hook.methodName}
                                                <div class="duration">
                                                    ${hook.durationFormatted}
                                                </div>
                                            </a>
                                        </div>
                                        <div id="beforeHookPanel${i}" class="collapse" role="tabpanel"
                                             aria-labelledby="beforeHookHeader${i}">
                                            <#if !hook.passed>
                                                <div class="medium-text-panel failed-content-color">
                                                    ${errorTitle}
                                                </div>
                                                <pre class="small-text-panel failed-content-color">${hook.errorLog?html?trim}</pre>
                                            </#if>
                                            <#if hook.logRecords?? && hook.logRecords?has_content>
                                                <div class="card card-body">
                                                    <pre class="small-text-panel">
                                                     <#compress>
                                                         <#list hook.logRecords as log>
                                                             ${log?html?trim}

                                                         </#list>
                                                     </#compress>
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

            <#if feature.beforeReportDetails?? && feature.beforeReportDetails?has_content>
                <div id="beforeFeatureParentPanel" role="tablist" aria-multiselectable="true"
                     class="card-collapse">
                    <div class="card card-plain">
                        <div class="card-header" id="beforeFeatureHeader">
                            <a data-toggle="collapse"
                               href="#beforeFeaturePanel" aria-expanded="false"
                               aria-controls="beforeFeaturePanel" data-parent="#beforeFeatureParentPanel"
                               class="collapsed <#if feature.passed>passed-color<#else>failed-color</#if>">
                                ${beforeTitle}
                                <div class="duration">
                                    ${feature.beforeFeatureDurationFormatted}
                                </div>
                            </a>
                        </div>

                        <div id="beforeFeaturePanel" role="tablist" aria-multiselectable="true"
                             class="collapse" aria-labelledby="beforeFeatureHeader">
                            <div class="card-body">
                                <#list feature.beforeReportDetails as step>
                                    <#assign i = step?index />
                                    <div class="card card-plain">
                                        <div class="card-header" id="beforeFeatureStepHeader${i}">
                                            <a data-toggle="collapse" data-parent="#testStepsPanel${i}"
                                               href="#beforeFeatureStepPanel${i}" aria-expanded="false"
                                               aria-controls="testStepsPanel${i}"
                                               class="collapsed <#if step.ignored>ignored-color<#elseif step.passed>passed-color<#else>failed-color</#if>">
                                                ${step.name}
                                                <div class="duration">
                                                    ${step.durationFormatted}
                                                </div>
                                            </a>
                                        </div>
                                        <div id="beforeFeatureStepPanel${i}" class="collapse" role="tabpanel"
                                             aria-labelledby="beforeFeatureStepHeader${i}">
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
                                                    <a data-toggle="collapse" data-parent="#beforeFeatureStepPanel${i}"
                                                       href="#beforeFeatureLogPanel${i}" aria-expanded="false"
                                                       aria-controls="beforeFeatureLogPanel${i}"
                                                       class="collapsed passed-color">
                                                        ${logTitle}
                                                    </a>
                                                </div>
                                                <div class="small-text-panel">
                                                    <div class="collapse" id="beforeFeatureLogPanel${i}">
                                                        <div class="card card-body">
                                                                <pre class="small-text-panel">
                                                            <#compress>
                                                                <#list step.logRecords as log>
                                                                    ${log?html?trim}
                                                                </#list>
                                                            </#compress>

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
            </#if>

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

                                <#if tsrd.beforeHooksReportDetails?? && tsrd.beforeHooksReportDetails?has_content>
                                    <div id="beforeScenarioHooksParentPanel${i}" role="tablist"
                                         aria-multiselectable="true"
                                         class="card-collapse">
                                        <div class="card card-plain">
                                            <div class="card-header" id="beforeScenarioHooksHeader${i}">
                                                <a data-toggle="collapse"
                                                   href="#beforeScenarioHooksPanel${i}" aria-expanded="false"
                                                   aria-controls="beforeScenarioHooksParentPanel${i}"
                                                   data-parent="#beforeScenarioHooksParentPanel${i}"
                                                   class="collapsed <#if tsrd.passed>passed-color<#else>failed-color</#if>">
                                                    ${hooksTitle}
                                                    <div class="duration">
                                                        ${tsrd.beforeHooksDurationFormatted}
                                                    </div>
                                                </a>
                                            </div>

                                            <div id="beforeScenarioHooksPanel${i}" role="tablist"
                                                 aria-multiselectable="true"
                                                 class="collapse" aria-labelledby="beforeScenarioHooksHeader${i}">
                                                <div class="card-body">
                                                    <#list tsrd.beforeHooksReportDetails as hook>
                                                        <#assign shi = hook?index />
                                                        <div class="card card-plain">
                                                            <div class="card-header"
                                                                 id="beforeScenarioHookHeader${i}_${shi}">
                                                                <a data-toggle="collapse"
                                                                   data-parent="#beforeScenarioHookPanel${i}_${shi}"
                                                                   href="#beforeScenarioHookPanel${i}_${shi}"
                                                                   aria-expanded="false"
                                                                   aria-controls="beforeScenarioHookPanel${i}_${shi}"
                                                                   class="collapsed <#if hook.passed>passed-color<#else>failed-color</#if>">
                                                                    ${hook.methodName}
                                                                    <div class="duration">
                                                                        ${hook.durationFormatted}
                                                                    </div>
                                                                </a>
                                                            </div>
                                                            <div id="beforeScenarioHookPanel${i}_${shi}"
                                                                 class="collapse" role="tabpanel"
                                                                 aria-labelledby="beforeScenarioHookHeader${i}_${shi}">
                                                                <#if !hook.passed>
                                                                    <div class="medium-text-panel failed-content-color">
                                                                        ${errorTitle}
                                                                    </div>
                                                                    <pre class="small-text-panel failed-content-color">${hook.errorLog?html?trim}</pre>
                                                                </#if>
                                                                <#if hook.logRecords?? && hook.logRecords?has_content>
                                                                    <div class="card card-body">
                                                                        <pre class="small-text-panel">
                                                                         <#compress>
                                                                             <#list hook.logRecords as log>
                                                                                 ${log?html?trim}
                                                                             </#list>
                                                                         </#compress>
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

                                <#if tsrd.beforeReportDetails?? && tsrd.beforeReportDetails?has_content>
                                    <div id="beforeScenarioParentPanel${i}" role="tablist" aria-multiselectable="true"
                                         class="card-collapse">
                                        <div class="card card-plain">
                                            <div class="card-header" id="beforeScenarioHeader${i}">
                                                <a data-toggle="collapse"
                                                   href="#beforeScenarioPanel${i}" aria-expanded="false"
                                                   aria-controls="beforeScenarioPanel${i}"
                                                   data-parent="#beforeScenarioParentPanel${i}"
                                                   class="collapsed <#if tsrd.passed>passed-color<#else>failed-color</#if>">
                                                    ${beforeTitle}
                                                    <div class="duration">
                                                        ${tsrd.beforeScenarioDurationFormatted}
                                                    </div>
                                                </a>
                                            </div>

                                            <div id="beforeScenarioPanel${i}" role="tablist" aria-multiselectable="true"
                                                 class="collapse" aria-labelledby="beforeScenarioHeader${i}">
                                                <div class="card-body">
                                                    <#list tsrd.beforeReportDetails as step>
                                                        <#assign bsi = step?index />
                                                        <div class="card card-plain">
                                                            <div class="card-header"
                                                                 id="beforeScenarioStepHeader${i}_${bsi}">
                                                                <a data-toggle="collapse"
                                                                   data-parent="#beforeScenarioStepPanel${i}_${bsi}"
                                                                   href="#beforeScenarioStepPanel${i}_${bsi}"
                                                                   aria-expanded="false"
                                                                   aria-controls="beforeScenarioStepPanel${i}_${bsi}"
                                                                   class="collapsed <#if step.ignored>ignored-color<#elseif step.passed>passed-color<#else>failed-color</#if>">
                                                                    ${step.name}
                                                                    <div class="duration">
                                                                        ${step.durationFormatted}
                                                                    </div>
                                                                </a>
                                                            </div>
                                                            <div id="beforeScenarioStepPanel${i}_${bsi}"
                                                                 class="collapse" role="tabpanel"
                                                                 aria-labelledby="beforeScenarioStepHeader${bsi}">
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
                                                                        <a data-toggle="collapse"
                                                                           data-parent="#beforeScenarioStepPanel${i}_${bsi}"
                                                                           href="#beforeScenarioLogPanel${i}_${bsi}"
                                                                           aria-expanded="false"
                                                                           aria-controls="beforeScenarioLogPanel${i}_${bsi}"
                                                                           class="collapsed passed-color">
                                                                            ${logTitle}
                                                                        </a>
                                                                    </div>
                                                                    <div class="small-text-panel">
                                                                        <div class="collapse"
                                                                             id="beforeScenarioLogPanel${i}_${bsi}">
                                                                            <div class="card card-body">
                                                                            <pre class="small-text-panel">
                                                                             <#compress>
                                                                                 <#list step.logRecords as log>
                                                                                     ${log?html?trim}
                                                                                 </#list>
                                                                             </#compress>
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
                                </#if>

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
                                                                 <#compress>
                                                                     <#list step.logRecords as log>
                                                                         ${log?html?trim}
                                                                     </#list>
                                                                 </#compress>
                                                                </pre>
                                                        </div>
                                                    </div>
                                                </div>
                                            </#if>
                                        </div>
                                    </div>
                                </#list>

                                <#if tsrd.afterReportDetails?? && tsrd.afterReportDetails?has_content>
                                    <div id="afterScenarioParentPanel${i}" role="tablist" aria-multiselectable="true"
                                         class="card-collapse">
                                        <div class="card card-plain">
                                            <div class="card-header" id="afterScenarioHeader${i}">
                                                <a data-toggle="collapse"
                                                   href="#afterScenarioPanel${i}" aria-expanded="false"
                                                   aria-controls="afterScenarioStepsPanel${i}"
                                                   data-parent="#afterScenarioParentPanel${i}"
                                                   class="collapsed <#if tsrd.passed>passed-color<#else>failed-color</#if>">
                                                    ${afterTitle}
                                                    <div class="duration">
                                                        ${tsrd.afterScenarioDurationFormatted}
                                                    </div>
                                                </a>
                                            </div>

                                            <div id="afterScenarioPanel${i}" role="tablist" aria-multiselectable="true"
                                                 class="collapse" aria-labelledby="afterScenarioHeader${i}">
                                                <div class="card-body">
                                                    <#list tsrd.afterReportDetails as step>
                                                        <#assign asi = step?index />
                                                        <div class="card card-plain">
                                                            <div class="card-header"
                                                                 id="afterScenarioStepHeader${i}_${asi}">
                                                                <a data-toggle="collapse"
                                                                   data-parent="#afterScenarioStepPanel${i}_${asi}"
                                                                   href="#afterScenarioStepPanel${i}_${asi}"
                                                                   aria-expanded="false"
                                                                   aria-controls="afterScenarioStepPanel${i}_${asi}"
                                                                   class="collapsed <#if step.ignored>ignored-color<#elseif step.passed>passed-color<#else>failed-color</#if>">
                                                                    ${step.name}
                                                                    <div class="duration">
                                                                        ${step.durationFormatted}
                                                                    </div>
                                                                </a>
                                                            </div>
                                                            <div id="afterScenarioStepPanel${i}_${asi}" class="collapse"
                                                                 role="tabpanel"
                                                                 aria-labelledby="afterScenarioStepHeader${asi}">
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
                                                                        <a data-toggle="collapse"
                                                                           data-parent="#afterScenarioStepPanel${i}_${asi}"
                                                                           href="#afterScenarioLogPanel${i}_${asi}"
                                                                           aria-expanded="false"
                                                                           aria-controls="afterScenarioLogPanel${i}_${asi}"
                                                                           class="collapsed passed-color">
                                                                            ${logTitle}
                                                                        </a>
                                                                    </div>
                                                                    <div class="small-text-panel">
                                                                        <div class="collapse"
                                                                             id="afterScenarioLogPanel${i}_${asi}">
                                                                            <div class="card card-body">
                                                                            <pre class="small-text-panel">
                                                                             <#compress>
                                                                                 <#list step.logRecords as log>
                                                                                     ${log?html?trim}
                                                                                 </#list>
                                                                             </#compress>
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
                                </#if>

                                <#if tsrd.afterHooksReportDetails?? && tsrd.afterHooksReportDetails?has_content>
                                    <div id="afterScenarioHooksParentPanel${i}" role="tablist"
                                         aria-multiselectable="true"
                                         class="card-collapse">
                                        <div class="card card-plain">
                                            <div class="card-header" id="afterScenarioHooksHeader${i}">
                                                <a data-toggle="collapse"
                                                   href="#afterScenarioHooksPanel${i}" aria-expanded="false"
                                                   aria-controls="afterScenarioHooksPanel${i}"
                                                   data-parent="#afterScenarioHooksParentPanel${i}"
                                                   class="collapsed <#if tsrd.passed>passed-color<#else>failed-color</#if>">
                                                    ${hooksTitle}
                                                    <div class="duration">
                                                        ${tsrd.afterHooksDurationFormatted}
                                                    </div>
                                                </a>
                                            </div>

                                            <div id="afterScenarioHooksPanel${i}" role="tablist"
                                                 aria-multiselectable="true"
                                                 class="collapse" aria-labelledby="afterScenarioHooksHeader${i}">
                                                <div class="card-body">
                                                    <#list tsrd.afterHooksReportDetails as hook>
                                                        <#assign shi = hook?index />
                                                        <div class="card card-plain">
                                                            <div class="card-header"
                                                                 id="afterScenarioHookHeader${i}_${shi}">
                                                                <a data-toggle="collapse"
                                                                   data-parent="#afterScenarioHookPanel${i}_${shi}"
                                                                   href="#afterScenarioHookPanel${i}_${shi}"
                                                                   aria-expanded="false"
                                                                   aria-controls="afterScenarioHookPanel${i}_${shi}"
                                                                   class="collapsed <#if hook.passed>passed-color<#else>failed-color</#if>">
                                                                    ${hook.methodName}
                                                                    <div class="duration">
                                                                        ${hook.durationFormatted}
                                                                    </div>
                                                                </a>
                                                            </div>
                                                            <div id="afterScenarioHookPanel${i}_${shi}" class="collapse"
                                                                 role="tabpanel"
                                                                 aria-labelledby="afterScenarioHookHeader${i}_${shi}">
                                                                <#if !hook.passed>
                                                                    <div class="medium-text-panel failed-content-color">
                                                                        ${errorTitle}
                                                                    </div>
                                                                    <pre class="small-text-panel failed-content-color">${hook.errorLog?html?trim}</pre>
                                                                </#if>
                                                                <#if hook.logRecords?? && hook.logRecords?has_content>
                                                                    <div class="card card-body">
                                                                        <pre class="small-text-panel">
                                                                         <#compress>
                                                                             <#list hook.logRecords as log>
                                                                                 ${log?html?trim}
                                                                             </#list>
                                                                         </#compress>
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

                        </div>
                    </div>

                </div>
            </#list>

            <#if feature.afterReportDetails?? && feature.afterReportDetails?has_content>
                <div id="afterFeatureParentPanel" role="tablist" aria-multiselectable="true"
                     class="card-collapse">
                    <div class="card card-plain">
                        <div class="card-header" id="afterFeatureHeader">
                            <a data-toggle="collapse"
                               href="#afterFeaturePanel" aria-expanded="false"
                               aria-controls="testStepsPanel" data-parent="#afterFeatureParentPanel"
                               class="collapsed <#if feature.passed>passed-color<#else>failed-color</#if>">
                                ${afterTitle}
                                <div class="duration">
                                    ${feature.afterFeatureDurationFormatted}
                                </div>
                            </a>
                        </div>

                        <div id="afterFeaturePanel" role="tablist" aria-multiselectable="true"
                             class="collapse" aria-labelledby="afterFeatureHeader">
                            <div class="card-body">
                                <#list feature.afterReportDetails as step>
                                    <#assign i = step?index />
                                    <div class="card card-plain">
                                        <div class="card-header" id="afterFeatureStepHeader${i}">
                                            <a data-toggle="collapse" data-parent="#testStepsPanel${i}"
                                               href="#afterFeatureStepPanel${i}" aria-expanded="false"
                                               aria-controls="testStepsPanel${i}"
                                               class="collapsed <#if step.ignored>ignored-color<#elseif step.passed>passed-color<#else>failed-color</#if>">
                                                ${step.name}
                                                <div class="duration">
                                                    ${step.durationFormatted}
                                                </div>
                                            </a>
                                        </div>
                                        <div id="afterFeatureStepPanel${i}" class="collapse" role="tabpanel"
                                             aria-labelledby="afterFeatureStepHeader${i}">
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
                                                    <a data-toggle="collapse" data-parent="#afterFeatureStepPanel${i}"
                                                       href="#afterFeatureLogPanel${i}" aria-expanded="false"
                                                       aria-controls="afterFeatureLogPanel${i}"
                                                       class="collapsed passed-color">
                                                        ${logTitle}
                                                    </a>
                                                </div>
                                                <div class="small-text-panel">
                                                    <div class="collapse" id="afterFeatureLogPanel${i}">
                                                        <div class="card card-body">
                                                                <pre class="small-text-panel">
                                                                 <#compress>
                                                                     <#list step.logRecords as log>
                                                                         ${log?html?trim}
                                                                     </#list>
                                                                 </#compress>
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
            </#if>

            <#if feature.afterHooksReportDetails?? && feature.afterHooksReportDetails?has_content>
                <div id="afterHooksParentPanel" role="tablist" aria-multiselectable="true"
                     class="card-collapse">
                    <div class="card card-plain">
                        <div class="card-header" id="afterHooksHeader">
                            <a data-toggle="collapse"
                               href="#afterHooksPanel" aria-expanded="false"
                               aria-controls="afterHooksPanel" data-parent="#afterHooksParentPanel"
                               class="collapsed <#if feature.passed>passed-color<#else>failed-color</#if>">
                                ${hooksTitle}
                                <div class="duration">
                                    ${feature.afterHooksDurationFormatted}
                                </div>
                            </a>
                        </div>

                        <div id="afterHooksPanel" role="tablist" aria-multiselectable="true"
                             class="collapse" aria-labelledby="afterHooksHeader">
                            <div class="card-body">
                                <#list feature.afterHooksReportDetails as hook>
                                    <#assign i = hook?index />
                                    <div class="card card-plain">
                                        <div class="card-header" id="afterHookHeader${i}">
                                            <a data-toggle="collapse" data-parent="#afterHookPanel${i}"
                                               href="#afterHookPanel${i}" aria-expanded="false"
                                               aria-controls="afterHookPanel${i}"
                                               class="collapsed <#if hook.passed>passed-color<#else>failed-color</#if>">
                                                ${hook.methodName}
                                                <div class="duration">
                                                    ${hook.durationFormatted}
                                                </div>
                                            </a>
                                        </div>
                                        <div id="afterHookPanel${i}" class="collapse" role="tabpanel"
                                             aria-labelledby="afterHookHeader${i}">
                                            <#if !hook.passed>
                                                <div class="medium-text-panel failed-content-color">
                                                    ${errorTitle}
                                                </div>
                                                <pre class="small-text-panel failed-content-color">${hook.errorLog?html?trim}</pre>
                                            </#if>
                                            <#if hook.logRecords?? && hook.logRecords?has_content>
                                                <div class="card card-body">
                                                    <pre class="small-text-panel">
                                                     <#compress>
                                                         <#list hook.logRecords as log>
                                                             ${log?html?trim}
                                                         </#list>
                                                     </#compress>
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
    </div>
</div>
</body>
</html>