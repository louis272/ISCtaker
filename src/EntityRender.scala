/**
 * EntityRender class
 * This class generalizes the rendering of entities.
 *
 * @param frames List of frames
 * @param scale Scale of the entity
 */
class EntityRender(var frames: List[String], var scale : Double) {
  /**
   * Constructor for EntityRender
   *
   * @param scale Scale of the entity
   */
  def this(scale: Double) = this(List(), scale)
}