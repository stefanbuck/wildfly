/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.clustering.infinispan.subsystem.remote;

import org.infinispan.client.hotrod.configuration.Configuration;
import org.jboss.as.clustering.controller.BinaryRequirementServiceNameFactory;
import org.jboss.as.clustering.controller.BinaryServiceNameFactory;
import org.jboss.as.clustering.controller.BinaryServiceNameFactoryProvider;
import org.jboss.as.clustering.controller.CapabilityReference;
import org.jboss.as.clustering.controller.ChildResourceDefinition;
import org.jboss.as.clustering.controller.CommonUnaryRequirement;
import org.jboss.as.clustering.controller.ManagementResourceRegistration;
import org.jboss.as.clustering.controller.ResourceDescriptor;
import org.jboss.as.clustering.controller.ResourceServiceBuilderFactory;
import org.jboss.as.clustering.controller.RestartParentResourceRegistration;
import org.jboss.as.clustering.infinispan.subsystem.InfinispanExtension;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.CapabilityReferenceRecorder;
import org.jboss.as.controller.ModelVersion;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.StringListAttributeDefinition;
import org.jboss.as.controller.capability.RuntimeCapability;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.transform.description.ResourceTransformationDescriptionBuilder;
import org.wildfly.clustering.service.BinaryRequirement;

/**
 * /subsystem=infinispan/remote-cache-container=X/remote-cluster=Y
 *
 * @author Radoslav Husar
 */
public class RemoteClusterResourceDefinition extends ChildResourceDefinition<ManagementResourceRegistration> {

    public static final PathElement WILDCARD_PATH = pathElement(PathElement.WILDCARD_VALUE);

    public static PathElement pathElement(String name) {
        return PathElement.pathElement("remote-cluster", name);
    }

    public enum Attribute implements org.jboss.as.clustering.controller.Attribute {
        SOCKET_BINDINGS("socket-bindings", new CapabilityReference(Capability.REMOTE_CLUSTER, CommonUnaryRequirement.OUTBOUND_SOCKET_BINDING)),
        ;

        private final AttributeDefinition definition;

        Attribute(String name, CapabilityReferenceRecorder reference) {
            this.definition = new StringListAttributeDefinition.Builder(name)
                    .setAllowExpression(false)
                    .setRequired(true)
                    .setCapabilityReference(reference)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .build();
        }

        @Override
        public AttributeDefinition getDefinition() {
            return this.definition;
        }
    }

    enum Requirement implements BinaryRequirement, BinaryServiceNameFactoryProvider {
        REMOTE_CLUSTER("org.wildfly.clustering.infinispan.remote-cache-container.remote-cluster", Void.class),
        ;
        private final String name;
        private final Class<?> type;
        private final BinaryServiceNameFactory factory = new BinaryRequirementServiceNameFactory(this);

        Requirement(String name, Class<?> type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public Class<?> getType() {
            return this.type;
        }

        @Override
        public BinaryServiceNameFactory getServiceNameFactory() {
            return this.factory;
        }
    }


    enum Capability implements org.jboss.as.clustering.controller.Capability {
        REMOTE_CLUSTER("org.wildfly.clustering.infinispan.remote-cache-container.remote-cluster", Void.class),
        ;
        private final RuntimeCapability<Void> definition;

        Capability(String name, Class<?> type) {
            this.definition = RuntimeCapability.Builder.of(name, true, type).build();
        }

        @Override
        public RuntimeCapability<Void> getDefinition() {
            return this.definition;
        }

        @Override
        public RuntimeCapability<Void> resolve(PathAddress address) {
            return this.definition.fromBaseCapability(address.getParent().getLastElement().getValue(), address.getLastElement().getValue());
        }
    }

    private final ResourceServiceBuilderFactory<Configuration> builderFactory;

    RemoteClusterResourceDefinition(ResourceServiceBuilderFactory<Configuration> builderFactory) {
        super(WILDCARD_PATH, InfinispanExtension.SUBSYSTEM_RESOLVER.createChildResolver(WILDCARD_PATH));
        this.builderFactory = builderFactory;
    }

    @Override
    public ManagementResourceRegistration register(ManagementResourceRegistration parentRegistration) {
        ManagementResourceRegistration registration = parentRegistration.registerSubModel(this);

        ResourceDescriptor descriptor = new ResourceDescriptor(this.getResourceDescriptionResolver())
                .addAttributes(Attribute.class)
                .addCapabilities(Capability.class);
        new RestartParentResourceRegistration<>(builderFactory, descriptor).register(registration);

        return registration;
    }

    static void buildTransformation(ModelVersion version, ResourceTransformationDescriptionBuilder parent) {
        // Nothing to transform yet
    }
}
