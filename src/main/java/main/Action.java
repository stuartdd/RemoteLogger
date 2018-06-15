/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

/**
 *
 * @author stuart
 */
public enum Action {
    SERVER_START,
    SERVER_STOPPING,
    SERVER_STOP,
    SERVER_FAIL,
    LOG,
    LOG_BODY,
    LOG_HEADER,
    LOG_REFRESH,
    PORT_NUMBER_ERROR,
    CONFIG_SAVE_ERROR,
    CLEAR_LOGS,
    SCROLL_TO_END
}
