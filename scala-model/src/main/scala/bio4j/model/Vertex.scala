package bio4j.model

/*
  `AnyVertex` defines a denotation of the corresponding `VertexType`.

  Instances are modeled as instances of other type tagged with the singleton type of a `Vertex`. For example, an instance of a self of type `User` when stored/represented by a `Neo4jNode` is going to be something of type `FieldType[user.type, Neo4jNode]`  where `user.type <: AnyVertex { type VertexType = User.type; type Rep = Neo4jNode }`.
  
  They are designed to be compatible with shapeless records (maybe, we'll see).
*/
trait AnyVertex { self =>
  
  type Tpe <: AnyVertexType
  val  tpe: Tpe

  type Rep

  import vertexTags._
  // TODO add to TaggedRep the vertex type
  type TaggedRep = VertexRepType[self.type]
  def ->>(r: Rep): VertexRepType[self.type] = vrep[self.type](r)

  /* Read a property from this representation */
  import SmthHasProperty._

  trait AnyGetProperty {
    type Property <: AnyProperty
    val p: Property

    def apply(rep: self.TaggedRep): Property#Rep
  }
  abstract class GetProperty[P <: AnyProperty](val p: P) extends AnyGetProperty {

    type Property = P
  }

  implicit def propertyOps(rep: self.TaggedRep): PropertyOps = PropertyOps(rep)
  case class   PropertyOps(rep: self.TaggedRep) {

    def get[P <: AnyProperty: PropertyOf[self.Tpe]#is](p: P)
      (implicit mkGetter: P => GetProperty[P]): P#Rep = {

        val g: GetProperty[P] = mkGetter(p)
        g(rep)
      }

  }

  /* If have just an independent getter for a particular property: */
  implicit def idGetter[P <: AnyProperty: PropertyOf[self.Tpe]#is](p: P)
      (implicit getter: GetProperty[P]) = getter


  /* Getters for incoming/outgoing edges */
  abstract case class RetrieveOutEdge[E <: AnyEdge](val r: E) {
    def apply(rep: self.TaggedRep): r.tpe.Out[r.Rep]
  }
  abstract case class RetrieveInEdge[E <: AnyEdge](val r: E) {
    def apply(rep: self.TaggedRep): r.tpe.In[r.Rep]
  }

  implicit def vertexOps(rep: self.TaggedRep) = VertexOps(rep)
  case class   VertexOps(rep: self.TaggedRep) {

    def out[E <: AnyEdge.withSourceType[self.Tpe]]
      (e: E)(implicit retrieve: RetrieveOutEdge[E]) = retrieve(rep)

    def in[E <: AnyEdge.withTargetType[self.Tpe]]
      (e: E)(implicit retrieve: RetrieveInEdge[E]) = retrieve(rep)

  }

}

abstract class Vertex[VT <: AnyVertexType](val tpe: VT) 
  extends AnyVertex { type Tpe = VT }

object AnyVertex {
  type ofType[VT <: AnyVertexType] = AnyVertex { type Tpe = VT }
}