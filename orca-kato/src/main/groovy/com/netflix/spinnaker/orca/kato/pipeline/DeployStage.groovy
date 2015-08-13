/*
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.orca.kato.pipeline

import com.netflix.spinnaker.orca.kato.pipeline.strategy.DeployStrategyStage
import com.netflix.spinnaker.orca.kato.tasks.JarDiffsTask
import com.netflix.spinnaker.orca.pipeline.model.Stage
import groovy.transform.CompileStatic
import com.google.common.annotations.VisibleForTesting
import com.netflix.spinnaker.orca.kato.tasks.CreateDeployTask
import com.netflix.spinnaker.orca.kato.tasks.MonitorKatoTask
import com.netflix.spinnaker.orca.kato.tasks.ServerGroupCacheForceRefreshTask
import com.netflix.spinnaker.orca.kato.tasks.WaitForUpInstancesTask
import com.netflix.spinnaker.orca.igor.tasks.GetCommitsTask
import org.springframework.batch.core.Step
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@CompileStatic
class DeployStage extends DeployStrategyStage {
  public static final String PIPELINE_CONFIG_TYPE = "linearDeploy"

  DeployStage() {
    super(PIPELINE_CONFIG_TYPE)
  }

  @Autowired(required = false)
  GetCommitsTask commitsTask

  @Autowired(required = false)
  JarDiffsTask jarDiffsTask

  @VisibleForTesting
  @Override
  protected List<Step> basicSteps(Stage stage) {
    def steps = [
      buildStep(stage, "createDeploy", CreateDeployTask),
      buildStep(stage, "monitorDeploy", MonitorKatoTask)
    ]
    if (commitsTask) {
      steps << buildStep(stage, "getCommits", GetCommitsTask)
    }
    steps << buildStep(stage, "forceCacheRefresh", ServerGroupCacheForceRefreshTask)
    steps << buildStep(stage, "waitForUpInstances", WaitForUpInstancesTask)
    steps << buildStep(stage, "forceCacheRefresh", ServerGroupCacheForceRefreshTask)
    if(jarDiffsTask) {
      steps << buildStep(stage, "jarDiffs", JarDiffsTask)
    }
    return steps
  }
}
