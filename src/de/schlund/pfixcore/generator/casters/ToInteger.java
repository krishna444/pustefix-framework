/*
* This file is part of PFIXCORE.
*
* PFIXCORE is free software; you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* PFIXCORE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with PFIXCORE; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*
*/

package de.schlund.pfixcore.generator.casters;
import de.schlund.pfixcore.generator.*;
import de.schlund.pfixxml.*;
import de.schlund.util.statuscodes.*;
import java.util.*;

/**
 * ToInteger.java
 *
 *
 * Created: Thu Aug 16 01:07:36 2001
 *
 * @author <a href="mailto: "Jens Lautenbacher</a>
 *
 *
 */

public class ToInteger extends SimpleCheck implements IWrapperParamCaster {
    private Integer[]  value = null;
    private StatusCode scode;

    public ToInteger() {
        scode = StatusCodeFactory.getInstance().getStatusCode("pfixcore.generator.caster.ERR_TO_INTEGER");
    }

    public void put_scode_casterror(String fqscode) {
        scode = StatusCodeFactory.getInstance().getStatusCode(fqscode);
    }

    public Object[] getValue() {
        return value;
    }
    
    public void castValue(RequestParam[] param) {
        for (int i = 0; i < param.length; i++) {
            CAT.debug("*** IN param: " + param[i]);
        }
        reset();
        ArrayList out = new ArrayList();
        Integer   val;
        for (int i = 0; i < param.length; i++) {
            try {
                val = new Integer(param[i].getValue());
                out.add(val);
            } catch (NumberFormatException e) {
                val = null;
                addSCode(scode);
                break;
            }
        }
        if (!errorHappened()) {
            value = (Integer[]) out.toArray(new Integer[] {});
        }
    }
    
}// ToInteger
