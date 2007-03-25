/*******************************************************************************
 *   Copyright 2007 SIP Response
 *   Copyright 2007 Michael D. Cohen
 *
 *      mike _AT_ sipresponse.com
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 ******************************************************************************/

/*
 *   SOURCE FROM: NIST JAIN SIP PROJECT
 *   
 *   
 */

/******************************************************
 * File: AuthenticationMethod.java
 *
 *@author M. Ranganathan
 *
 */

package com.sipresponse.flibblecallmgr.internal.util;

import javax.sip.message.*;
import javax.sip.header.*;

public interface AuthenticationMethod {

	/**
	 * Get the authentication scheme
	 */
	public String getScheme();

	/**
	 * Initialize the authentication method. This has to be done outside the
	 * constructor as the constructor is generic (created from the class name
	 * specified in the authentication method).
	 */
	public void initialize();

	/**
	 * Get the authentication realm.
	 */
	public String getRealm(String resource);

	/**
	 * get the authentication domain.
	 */
	public String getDomain();

	/**
	 * Get the authentication Algorithm
	 */
	public String getAlgorithm();

	/**
	 * Generate the challenge string.
	 */
	public String generateNonce();

	/**
	 * Check the response and answer true if authentication succeeds. Not all of
	 * these fields are relevant for every method - a basic scheme may simply do
	 * a username password check.
	 * 
	 * @param username
	 *            is the username and password.
	 * @param authorizationHeader
	 *            is the authorization header from the SIP request.
	 * @param requestLine
	 *            is the RequestLine from the SIP Request.
	 */
	public boolean doAuthenticate(String username,
			AuthorizationHeader authorizationHeader, Request request);

}
