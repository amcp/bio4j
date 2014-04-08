package com.bio4j.model.go;

import java.util.Set;
import java.util.HashSet;

import com.bio4j.model.Module;
import com.bio4j.model.NodeType;
import com.bio4j.model.RelationshipType;
import com.bio4j.model.PropertyType;

import com.bio4j.model.go.nodes.*;
import com.bio4j.model.properties.*;
import com.bio4j.model.go.relationships.*;

public class go_module implements Module {
  
  public static String PKG = "com.bio4j.model.go";

  public static Set<NodeType> NODE_TYPES = new HashSet<NodeType>() {{

    add(
      GoTerm.TYPE
    );
  }};

  public static Set<RelationshipType> RELATIONSHIP_TYPES = new HashSet<RelationshipType>() {{

    add(
      IsA.TYPE                    
    ); add(
      PartOf.TYPE                 
    ); add(
      HasPartOf.TYPE
    ); add(
      NegativelyRegulates.TYPE
    ); add(
      PositivelyRegulates.TYPE
    ); add(
      Regulates.TYPE
    );
  }};

  public static Set<PropertyType> PROPERTY_TYPES = new HashSet<PropertyType>() {{

    add(
      name.TYPE
    ); add(
      id.TYPE
    ); add(
      definition.TYPE
    ); add(
      comment.TYPE
    ); add(
      obsolete.TYPE
    ); add(
      alternativeIds.TYPE
    );
  }};


  public String pkg() { return PKG; }
  public Set<Module> dependencies() { return null; }
  public Set<PropertyType> propertyTypes() { return PROPERTY_TYPES; }
  public Set<NodeType> nodeTypes() { return NODE_TYPES; }
  public Set<RelationshipType> relationshipTypes() { return RELATIONSHIP_TYPES; }

  // you should implement indexes in subclasses
}