/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
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
 * #L%
 */
/*
 * ModeShape (http://www.modeshape.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.modeshape.web.jcr.rest.handler;

import org.apache.felix.ipojo.annotations.Requires;
import org.modeshape.web.jcr.rest.RestHelper;
import org.modeshape.web.jcr.rest.model.RestRepositories;
import org.wisdom.api.annotations.Service;
import org.wisdom.api.http.Request;
import org.wisdom.jcr.modeshape.api.RepositoryManager;

import javax.jcr.Repository;
import javax.jcr.Value;
import java.util.ArrayList;
import java.util.List;

/**
 * An class which returns POJO-based rest model instances.
 *
 * @author Horia Chiorean (hchiorea@redhat.com)
 */
@Service(RestServerHandler.class)
public class RestServerHandlerImpl extends AbstractHandler implements RestServerHandler {

    @Requires
    RepositoryManager repositoryManager;

    /**
     * Returns the list of JCR repositories available on this server
     *
     * @param request the servlet request; may not be null
     * @return a list of available JCR repositories, as a {@link org.modeshape.web.jcr.rest.model.RestRepositories} instance.
     */
    @Override
    public RestRepositories getRepositories(Request request) {
        RestRepositories repositories = new RestRepositories();
        for (String repositoryName : getRepositoryManager().getJcrRepositoryNames()) {
            addRepository(request, repositories, repositoryName);
        }
        return repositories;
    }

    private void addRepository(Request request,
                               RestRepositories repositories,
                               String repositoryName) {
        RestRepositories.Repository repository = repositories.addRepository(repositoryName, RestHelper.urlFrom(request,
                repositoryName));
        try {
            Repository jcrRepository = getRepositoryManager().getRepository(repositoryName);
            repository.setActiveSessionsCount(((org.modeshape.jcr.api.Repository) jcrRepository).getActiveSessionsCount());
            for (String metadataKey : jcrRepository.getDescriptorKeys()) {
                Value[] descriptorValues = jcrRepository.getDescriptorValues(metadataKey);
                if (descriptorValues != null) {
                    List<String> values = new ArrayList<String>(descriptorValues.length);
                    for (Value descriptorValue : descriptorValues) {
                        values.add(descriptorValue.getString());
                    }
                    repository.addMetadata(metadataKey, values);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    protected RepositoryManager getRepositoryManager() {
        return repositoryManager;
    }
}
