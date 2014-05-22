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

package org.pustefixframework.webservices.jsonws;

import java.io.IOException;
import java.io.Writer;

public class SerializationContext {

    boolean doClassHinting;
    SerializerRegistry serReg;
    
    public SerializationContext(SerializerRegistry serReg,boolean doClassHinting) {
        this.serReg=serReg;
        this.doClassHinting=doClassHinting;
    }
    
    public Object serialize(Object obj) throws SerializationException {
        Serializer ser=serReg.getSerializer(obj.getClass());
        return ser.serialize(this,obj);
    }
    
    public void serialize(Object obj,Writer writer) throws SerializationException,IOException {
        Serializer ser=serReg.getSerializer(obj.getClass());
        ser.serialize(this,obj,writer);
    }
    
    public boolean doClassHinting() {
        return doClassHinting;
    }
    
    public String getClassHintPropertyName() {
        return "javaClass";
    }
    
}
