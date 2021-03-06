package com.bio4j.model.uniprot.edges;

import com.bio4j.model.uniprot.UniProtGraph;
import com.bio4j.model.uniprot.vertices.Book;
import com.bio4j.model.uniprot.vertices.City;
import com.bio4j.angulillos.UntypedGraph;

/**
* @author <a href="mailto:ppareja@era7.com">Pablo Pareja Tobes</a>
*/
public final class BookCity<I extends UntypedGraph<RV, RVT, RE, RET>, RV, RVT, RE, RET>
extends
UniProtGraph.UniProtEdge<
Book<I, RV, RVT, RE, RET>, UniProtGraph<I, RV, RVT, RE, RET>.BookType,
BookCity<I, RV, RVT, RE, RET>, UniProtGraph<I, RV, RVT, RE, RET>.BookCityType,
City<I, RV, RVT, RE, RET>, UniProtGraph<I, RV, RVT, RE, RET>.CityType,
I, RV, RVT, RE, RET
> {

  public BookCity(RE edge, UniProtGraph<I, RV, RVT, RE, RET>.BookCityType type) {

    super(edge, type);
  }

  @Override
  public BookCity<I, RV, RVT, RE, RET> self() {
    return this;
  }
}
