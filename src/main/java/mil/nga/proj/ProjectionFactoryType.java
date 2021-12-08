package mil.nga.proj;

/**
 * Projection factory retrieval types for coordinate projections and
 * transformations
 * 
 * @author osbornb
 */
public enum ProjectionFactoryType {

	/**
	 * Cached projections from previous retrievals
	 */
	CACHE,

	/**
	 * Well-Known Text Definition parsing and proj4j conversion
	 */
	DEFINITION,

	/**
	 * Well-Known Text Definition parsing, PROJ conversion, and proj4j creation
	 * 
	 * @since 1.1.0
	 */
	DEFINITION_PARAMETERS,

	/**
	 * proj4j creation from a well-known coordinate reference system name
	 */
	NAME,

	/**
	 * proj4j creation from proj4j projection parameters
	 */
	PARAMETERS,

	/**
	 * Creation from project and custom properties of configured proj4j
	 * projection parameters
	 */
	PROPERTIES;

}
