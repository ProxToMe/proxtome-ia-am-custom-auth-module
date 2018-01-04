/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2016 ForgeRock AS. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file at legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 */

/**
 * Portions Copyrighted 2017-2018 ProxToMe inc.
 */

package com.proxtome.ia.auth;

import java.io.Serializable;
import java.security.Principal;

/**
 * ProxToMeIAAuthPrincipal represents the user entity.
 */
public class ProxToMeIAAuthPrincipal implements Principal, Serializable {
    private final static String COLON = " : ";

    private final String name;

    public ProxToMeIAAuthPrincipal(String name) {

        if (name == null) {
            throw new NullPointerException("illegal null input");
        }

        this.name = name;
    }

    /**
     * Return the LDAP username for this <code>ProxToMeIAAuthPrincipal</code>.
     *
     * @return the LDAP username for this <code>ProxToMeIAAuthPrincipal</code>
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Return a string representation of this <code>ProxToMeIAAuthPrincipal</code>.
     *
     * @return a string representation of this
     *         <code>TestAuthModulePrincipal</code>.
     */
    @Override
    public String toString() {
        return new StringBuilder().append(this.getClass().getName())
                .append(COLON).append(name).toString();
    }

    /**
     * Compares the specified Object with this <code>ProxToMeIAAuthPrincipal</code>
     * for equality. Returns true if the given object is also a
     * <code> ProxToMeIAAuthPrincipal </code> and the two ProxToMeIAAuthPrincipal have
     * the same username.
     *
     * @param o Object to be compared for equality with this
     *          <code>ProxToMeIAAuthPrincipal</code>.
     * @return true if the specified Object is equal equal to this
     *         <code>ProxToMeIAAuthPrincipal</code>.
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (this == o) {
            return true;
        }

        if (!(o instanceof ProxToMeIAAuthPrincipal)) {
            return false;
        }
        ProxToMeIAAuthPrincipal that = (ProxToMeIAAuthPrincipal) o;

        if (this.getName().equals(that.getName())) {
            return true;
        }
        return false;
    }

    /**
     * Return a hash code for this <code>ProxToMeIAAuthPrincipal</code>.
     *
     * @return a hash code for this <code>ProxToMeIAAuthPrincipal</code>.
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
