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

package de.schlund.pfixxml.targets;
import java.lang.reflect.Constructor;

import org.apache.log4j.Logger;

import de.schlund.pfixcore.exception.PustefixRuntimeException;
import de.schlund.pfixxml.IncludeDocument;

/**
 * SPCacheFactory.java
 *
 *
 * Created: Mon Jul 23 17:06:56 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a> 
 * @author <a href="mailto:haecker@schlund.de">Joerg Haecker</a>      
 *  
 * This class realises the factory and the singleton pattern and implements the {@link FactoryInit}
 * interface. It is responsible to create and store the caches uses by the PUSTEFIX system. 
 * Currently PUSTEFIX uses one cache for the targets and one for the include-modules. The
 * properties of the caches are passed via the init-method of the FactoryInit-interface. 
 * If the propertries can't be interpreted correctly or the init-method is not called,
 * {@link LRUCache} initialised with default values will be returned.
 *
 */

public class SPCacheFactory {
    private final static Logger LOG = Logger.getLogger(SPCacheFactory.class);
    private static SPCacheFactory instance= new SPCacheFactory();

    private SPCache<Object, Object> targetCache;
    private SPCache<String, IncludeDocument> documentCache;

    private int targetCacheCapacity = 30;
    private int includeCacheCapacity = 30;
    
    private String targetCacheClass = LRUCache.class.getName();
    private String includeCacheClass = LRUCache.class.getName();
    
    private SPCacheFactory() {
    	init();
    }

    /**
     * Implemented from FactoryInit.
     */
    public void init() {
        targetCache = getCache(targetCacheClass, targetCacheCapacity);
        documentCache = getCache(includeCacheClass, includeCacheCapacity);
        if(LOG.isInfoEnabled()) {
        	LOG.info("SPCacheFactory initialized: ");
        	LOG.info("  TargetCache   : Class="+targetCache.getClass().getName()+" Capacity=" + targetCache.getCapacity() + " Size="+targetCache.getSize());
        	LOG.info("  DocumentCache : Class="+documentCache.getClass().getName()+" Capacity=" + documentCache.getCapacity() + " Size="+documentCache.getSize());
        }
    }

    @SuppressWarnings("unchecked")
    private <T1, T2> SPCache<T1, T2> getCache(String className, int capacity) {
        SPCache<T1, T2> retval= null;
        try {
            Constructor<? extends SPCache> constr = Class.forName(className).asSubclass(SPCache.class).getConstructor((Class[]) null);
            retval = constr.newInstance((Object[]) null);
            retval.createCache(capacity);
        } catch (Exception e) {
            LOG.error("unable to instantiate class [" + className + "]", e);
            throw new PustefixRuntimeException("Can't create TargetGenerator cache", e);
        }
        return retval;
    }

    /**
     * The getInstance method of a singleton.
     */
    public static SPCacheFactory getInstance() {
        return instance;
    }

    /**
     * Get the cache for targets.
     */
    public SPCache<Object, Object> getCache() {
    	return targetCache;
    }

    /**
     * Get the cache for include-modules.
     */
    public SPCache<String, IncludeDocument> getDocumentCache() {
    	return documentCache;
    }
    
    /**
     * To be used with care! If you need it, take care to throw away your old instance of SPCache retrieved 
     * through getCache() and getDocumentCache()!
     */
     public void reset() {
    	 init();
     }
     
     public void setTargetCacheCapacity(int targetCacheCapacity) {
    	 this.targetCacheCapacity = targetCacheCapacity;
     }
     
     public void setIncludeCacheCapacity(int includeCacheCapacity) {
    	 this.includeCacheCapacity = includeCacheCapacity;
     }
     
     public void setTargetCacheClass(String targetCacheClass) {
    	 this.targetCacheClass = targetCacheClass;
     }
     
     public void setIncludeCacheClass(String includeCacheClass) {
    	 this.includeCacheClass = includeCacheClass;
     }
    

} // SPCacheFactory
