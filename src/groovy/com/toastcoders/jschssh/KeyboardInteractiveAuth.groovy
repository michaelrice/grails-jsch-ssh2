package com.toastcoders.jschssh

import com.jcraft.jsch.UIKeyboardInteractive
import com.jcraft.jsch.UserInfo
import org.apache.log4j.Logger

/**
 * Created with IntelliJ IDEA.
 * User: Michael Rice
 * Twitter: @errr_
 * Website: http://www.errr-online.com/
 * Github: https://github.com/michaelrice
 * Date: 5/17/2014
 * Time: 3:13 PM
 * Licenses: MIT http://opensource.org/licenses/MIT
 */
class KeyboardInteractiveAuth implements UserInfo, UIKeyboardInteractive {

    /**
     * Create a log4j Logger.
     */
    private Logger log = Logger.getLogger(KeyboardInteractiveAuth)

    /**
     * The password to use.
     */
    private String password

    /**
     * Constructor
     */
    public KeyboardInteractiveAuth(String password) {
        this.password = password
    }

    /**
     * Sets the password to use for auth.
     *
     * @param password
     */
    public void setPassword(String password) {
        this.password = password
    }

    /**
     * Implemented method from UserInfo
     *
     * @return String representation of the password being used to login with.
     */
    public String getPassword() {
        log.trace("Asking for the password.")
        return password
    }

    /**
     * TODO what is this?
     *
     * @param str
     * @return Boolean false
     */
    public boolean promptYesNo(String str) {
        log.trace("Prompting yes/no with: ${str} and returning false.")
        return false
    }

    /**
     * TODO what is this?
     * @return
     */
    public String getPassphrase() {
        log.trace("GetPassPhrase. Returning null.")
        return null
    }

    /**
     * TODO what is this?
     *
     * @param message
     * @return
     */
    public boolean promptPassphrase(String message) {
        log.trace("PromptPassphrase with ${message} || returning true")
        return true
    }

    /**
     * TODO what is this?
     *
     * @param message
     * @return
     */
    public boolean promptPassword(String message) {
        log.trace("promptPassword: ${message}")
        return true
    }

    /**
     * TODO I dont think this is used, so we need to find out.
     *
     * @param message
     */
    public void showMessage(String message) {
        log.trace("showMessage: ${message}")
    }

    /**
     * Method used to set password when the server
     * asks us for it.
     *
     * @param destination
     * @param name
     * @param instruction
     * @param prompt
     * @param echo
     * @return
     */
    public String[] promptKeyboardInteractive(String destination,
                                              String name,
                                              String instruction,
                                              String[] prompt,
                                              boolean[] echo) {
        List response = []
        for (int i = 0; i < prompt.length; i++) {
            log.trace("prompt: ${prompt[i]}")
            prompt[i]?.trim()
            if (prompt[i]?.equalsIgnoreCase("password:")) {
                log.trace("The server is asking us for a password.")
                response[i] = password
            }
        }
        return response
    }
}