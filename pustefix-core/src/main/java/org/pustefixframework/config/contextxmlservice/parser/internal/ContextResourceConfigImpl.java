/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.pustefixframework.config.contextxmlservice.parser.internal;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.pustefixframework.config.contextxmlservice.ContextResourceConfig;
import org.pustefixframework.container.spring.beans.TenantAwareProperties;


/**
 * Stores configuration for a ContextResource
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ContextResourceConfigImpl implements ContextResourceConfig {
    
    private Class<?> resourceClass = null;
    private HashSet<Class<?>> interfaces = new HashSet<Class<?>>();
    private Properties props = new TenantAwareProperties();
    private String beanName;
    
    public ContextResourceConfigImpl(Class<?> clazz) {
        this.resourceClass = clazz;
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.ContextResourceConfig#getContextResourceClass()
     */
    public Class<?> getContextResourceClass() {
        return this.resourceClass;
    }
    
    public void addInterface(Class<?> clazz) {
        this.interfaces.add(clazz); 
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.ContextResourceConfig#getInterfaces()
     */
    public Set<Class<?>> getInterfaces() {
        return this.interfaces;
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.ContextResourceConfig#getProperties()
     */
    public Properties getProperties() {
        return this.props;
    }
    
    public void setProperties(Properties properties) {
        this.props = properties;
    }
    
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }
    
    public String getBeanName() {
        return beanName;
    }
    
}
