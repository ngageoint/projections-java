package mil.nga.proj;

import org.junit.Test;
import org.locationtech.proj4j.ProjCoordinate;

import junit.framework.TestCase;

/**
 * README example tests
 * 
 * @author osbornb
 */
public class ReadmeTest {

	/**
	 * Test transform
	 */
	@Test
	public void testTransform() {

		ProjCoordinate transformed = testTransform(
				new ProjCoordinate(111319.49079327357, 111325.14286638486));

		TestCase.assertEquals(1.0, transformed.x, .0000000000001);
		TestCase.assertEquals(1.0, transformed.y, .0000000000001);

	}

	/**
	 * Test transform
	 * 
	 * @param coordinate
	 *            projection coordinate
	 * @return projection coordinate
	 */
	private ProjCoordinate testTransform(ProjCoordinate coordinate) {

		// ProjCoordinate coordinate = ...

		Projection projection1 = ProjectionFactory.getProjection(
				ProjectionConstants.AUTHORITY_EPSG,
				ProjectionConstants.EPSG_WEB_MERCATOR);
		Projection projection2 = ProjectionFactory.getProjection("EPSG:4326");
		Projection projection3 = ProjectionFactory.getProjection(
				ProjectionConstants.AUTHORITY_EPSG, 3123,
				"+proj=tmerc +lat_0=0 +lon_0=121 +k=0.99995 +x_0=500000 +y_0=0 +ellps=clrk66 "
						+ "+towgs84=-127.62,-67.24,-47.04,-3.068,4.903,1.578,-1.06 +units=m +no_defs");
		Projection projection4 = ProjectionFactory.getProjectionByDefinition(
				"PROJCS[\"Lambert_Conformal_Conic (1SP)\","
						+ "GEODCRS[\"GCS_North_American_1983\","
						+ "DATUM[\"North_American_Datum_1983\","
						+ "SPHEROID[\"GRS_1980\",6371000,0]],"
						+ "PRIMEM[\"Greenwich\",0],"
						+ "UNIT[\"Degree\",0.017453292519943295]],"
						+ "PROJECTION[\"Lambert_Conformal_Conic_1SP\"],"
						+ "PARAMETER[\"latitude_of_origin\",25],"
						+ "PARAMETER[\"central_meridian\",-95],"
						+ "PARAMETER[\"scale_factor\",1],"
						+ "PARAMETER[\"false_easting\",0],"
						+ "PARAMETER[\"false_northing\",0],"
						+ "PARAMETER[\"standard_parallel_1\",25],"
						+ "UNIT[\"Meter\",1],AUTHORITY[\"EPSG\",\"9801\"]]");

		ProjectionTransform transform = projection1
				.getTransformation(projection2);
		ProjectionTransform inverseTransform = transform
				.getInverseTransformation();

		ProjCoordinate transformed = transform.transform(coordinate);
		ProjCoordinate inverseTransformed = inverseTransform
				.transform(transformed);

		return transformed;
	}

}
