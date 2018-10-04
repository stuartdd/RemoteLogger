/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package expectations;

/**
 *
 * @author 802996013
 */
public class Exp {

    public static ExpChain withPath(String path) {
        return new ExpChain().withPath(path);
    }

    public static ExpChain withGetMethod() {
        return new ExpChain().withGetMethod();
    }
    
    public static ExpChain withName(String name) {
        return new ExpChain().withName(name);
    }

    public static ExpChain withAnyMethod() {
        return new ExpChain().withAnyMethod();
    }

    public static ExpChain withPostMethod() {
        return new ExpChain().withPostMethod();
    }

    public static ExpChain withPutMethod() {
        return new ExpChain().withPutMethod();
    }

    public static ExpChain withPatchMethod() {
        return new ExpChain().withPatchMethod();
    }

    public static ExpChain withDeleteMethod() {
        return new ExpChain().withDeleteMethod();
    }

    public static ExpChain withEmptyBody() {
        return new ExpChain().withEmptyBody();
    }

    public static ExpChain withJsonBody() {
        return new ExpChain().withJsonBody();
    }

    public static ExpChain withXmlBody() {
        return new ExpChain().withXmlBody();
    }
}
