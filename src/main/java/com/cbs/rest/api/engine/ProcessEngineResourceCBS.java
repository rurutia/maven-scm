/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cbs.rest.api.engine;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineInfo;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.restlet.resource.Get;

/**
 * Process engine info contains name indicating the rest war
 * is customized by CBS
 * Used to check if REST engine is running
 *
 */
public class ProcessEngineResourceCBS extends SecuredResource {

  @Get
  public ProcessEngineInfoResponseCBS getEngineInfo() {
    if(authenticate() == false) return null;
    
    ProcessEngineInfoResponseCBS response = new ProcessEngineInfoResponseCBS();
    
    ProcessEngineInfo engineInfo = ActivitiUtil.getProcessEngineInfo();
    if(engineInfo != null) {
      response.setName(engineInfo.getName() + "(customized by CBS)");
      response.setResourceUrl(engineInfo.getResourceUrl());
      response.setException(engineInfo.getException());
    } else {
      // Revert to using process-engine directly
      ProcessEngine engine = ActivitiUtil.getProcessEngine();
      response.setName(engine.getName());
    }
   
    response.setVersion(ProcessEngine.VERSION);
    return response;
  }
  
}