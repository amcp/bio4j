
package com.bio4j.model.nodes.citation;

import com.bio4j.model.Vertex;
import java.util.List;

/**
 *
 * @author Pablo Pareja Tobes <ppareja@era7.com>
 */
public interface Publisher extends Vertex {
        
    //----GETTERS---
    public String getName();
    public List<Book> getBooks();

    //----SETTERS---
    public void setName(String value);
}