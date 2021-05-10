package mil.nga.proj.crs.wkt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import mil.nga.proj.crs.CoordinateReferenceSystem;
import mil.nga.proj.crs.CoordinateReferenceSystemType;
import mil.nga.proj.crs.common.Axis;
import mil.nga.proj.crs.common.AxisDirectionType;
import mil.nga.proj.crs.common.CoordinateSystem;
import mil.nga.proj.crs.common.CoordinateSystemType;
import mil.nga.proj.crs.common.DatumEnsemble;
import mil.nga.proj.crs.common.Dynamic;
import mil.nga.proj.crs.common.Extent;
import mil.nga.proj.crs.common.GeographicBoundingBox;
import mil.nga.proj.crs.common.Identifier;
import mil.nga.proj.crs.common.TemporalExtent;
import mil.nga.proj.crs.common.Unit;
import mil.nga.proj.crs.common.UnitType;
import mil.nga.proj.crs.common.Usage;
import mil.nga.proj.crs.common.VerticalExtent;
import mil.nga.proj.crs.engineering.EngineeringCoordinateReferenceSystem;
import mil.nga.proj.crs.geodetic.Ellipsoid;
import mil.nga.proj.crs.geodetic.GeodeticCoordinateReferenceSystem;
import mil.nga.proj.crs.geodetic.GeodeticDatumEnsemble;
import mil.nga.proj.crs.geodetic.GeodeticReferenceFrame;
import mil.nga.proj.crs.geodetic.PrimeMeridian;
import mil.nga.proj.crs.parametric.ParametricCoordinateReferenceSystem;
import mil.nga.proj.crs.projected.MapProjection;
import mil.nga.proj.crs.projected.ProjectedCoordinateReferenceSystem;
import mil.nga.proj.crs.temporal.TemporalCoordinateReferenceSystem;
import mil.nga.proj.crs.temporal.TemporalDatum;
import mil.nga.proj.crs.vertical.VerticalCoordinateReferenceSystem;
import mil.nga.proj.crs.vertical.VerticalReferenceFrame;

/**
 * CRS Reader and Writer tests
 * 
 * @author osbornb
 */
public class CRSReaderWriterTest {

	/**
	 * Test scope
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testScope() throws IOException {

		String text = "SCOPE[\"Large scale topographic mapping and cadastre.\"]";
		CRSReader reader = new CRSReader(text);
		String scope = reader.readScope();
		assertNotNull(scope);
		assertEquals("Large scale topographic mapping and cadastre.", scope);
		reader.close();
		CRSWriter writer = new CRSWriter();
		writer.writeScope(scope);
		assertEquals(text, writer.toString());
		writer.close();

	}

	/**
	 * Test area description
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testAreaDescription() throws IOException {

		String text = "AREA[\"Netherlands offshore.\"]";
		CRSReader reader = new CRSReader(text);
		String areaDescription = reader.readAreaDescription();
		assertNotNull(areaDescription);
		assertEquals("Netherlands offshore.", areaDescription);
		reader.close();
		CRSWriter writer = new CRSWriter();
		writer.writeAreaDescription(areaDescription);
		assertEquals(text, writer.toString());
		writer.close();

	}

	/**
	 * Test geographic bounding box
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testGeographicBoundingBox() throws IOException {

		String text = "BBOX[51.43,2.54,55.77,6.40]";
		CRSReader reader = new CRSReader(text);
		GeographicBoundingBox boundingBox = reader.readGeographicBoundingBox();
		assertNotNull(boundingBox);
		assertEquals(51.43, boundingBox.getLowerLeftLatitude(), 0);
		assertEquals(2.54, boundingBox.getLowerLeftLongitude(), 0);
		assertEquals(55.77, boundingBox.getUpperRightLatitude(), 0);
		assertEquals(6.40, boundingBox.getUpperRightLongitude(), 0);
		reader.close();
		assertEquals(text.replaceAll("\\.40", ".4"), boundingBox.toString());

		text = "BBOX[-55.95,160.60,-25.88,-171.20]";
		reader = new CRSReader(text);
		boundingBox = reader.readGeographicBoundingBox();
		assertNotNull(boundingBox);
		assertEquals(-55.95, boundingBox.getLowerLeftLatitude(), 0);
		assertEquals(160.60, boundingBox.getLowerLeftLongitude(), 0);
		assertEquals(-25.88, boundingBox.getUpperRightLatitude(), 0);
		assertEquals(-171.20, boundingBox.getUpperRightLongitude(), 0);
		reader.close();
		assertEquals(text.replaceAll("\\.60", ".6").replaceAll("\\.20", ".2"),
				boundingBox.toString());

	}

	/**
	 * Test vertical extent
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testVerticalExtent() throws IOException {

		String text = "VERTICALEXTENT[-1000,0,LENGTHUNIT[\"metre\",1.0]]";
		CRSReader reader = new CRSReader(text);
		VerticalExtent verticalExtent = reader.readVerticalExtent();
		assertNotNull(verticalExtent);
		assertEquals(-1000, verticalExtent.getMinimumHeight(), 0);
		assertEquals(0, verticalExtent.getMaximumHeight(), 0);
		Unit lengthUnit = verticalExtent.getUnit();
		assertNotNull(lengthUnit);
		assertEquals(UnitType.LENGTHUNIT, lengthUnit.getType());
		assertEquals("metre", lengthUnit.getName());
		assertEquals(1.0, lengthUnit.getConversionFactor(), 0);
		reader.close();
		text = text.replaceAll("-1000,0", "-1000.0,0.0");
		assertEquals(text, verticalExtent.toString());

		text = "VERTICALEXTENT[-1000,0]";
		reader = new CRSReader(text);
		verticalExtent = reader.readVerticalExtent();
		assertNotNull(verticalExtent);
		assertEquals(-1000, verticalExtent.getMinimumHeight(), 0);
		assertEquals(0, verticalExtent.getMaximumHeight(), 0);
		lengthUnit = verticalExtent.getUnit();
		assertNull(lengthUnit);
		reader.close();
		text = text.replaceAll("-1000,0", "-1000.0,0.0");
		assertEquals(text, verticalExtent.toString());

	}

	/**
	 * Test temporal extent
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testTemporalExtent() throws IOException {

		String text = "TIMEEXTENT[2013-01-01,2013-12-31]";
		CRSReader reader = new CRSReader(text);
		TemporalExtent temporalExtent = reader.readTemporalExtent();
		assertNotNull(temporalExtent);
		assertEquals("2013-01-01", temporalExtent.getStart());
		assertTrue(temporalExtent.hasStartDateTime());
		assertEquals("2013-01-01",
				temporalExtent.getStartDateTime().toString());
		assertEquals("2013-12-31", temporalExtent.getEnd());
		assertTrue(temporalExtent.hasEndDateTime());
		assertEquals("2013-12-31", temporalExtent.getEndDateTime().toString());
		reader.close();
		assertEquals(text, temporalExtent.toString());

		text = "TIMEEXTENT[\"Jurassic\",\"Quaternary\"]";
		reader = new CRSReader(text);
		temporalExtent = reader.readTemporalExtent();
		assertNotNull(temporalExtent);
		assertEquals("Jurassic", temporalExtent.getStart());
		assertFalse(temporalExtent.hasStartDateTime());
		assertEquals("Quaternary", temporalExtent.getEnd());
		assertFalse(temporalExtent.hasEndDateTime());
		reader.close();
		assertEquals(text, temporalExtent.toString());

	}

	/**
	 * Test usage
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testUsage() throws IOException {

		String text = "USAGE[SCOPE[\"Spatial referencing.\"],"
				+ "AREA[\"Netherlands offshore.\"],TIMEEXTENT[1976-01,2001-04]]";
		CRSReader reader = new CRSReader(text);
		Usage usage = reader.readUsage();
		assertNotNull(usage);
		assertEquals("Spatial referencing.", usage.getScope());
		Extent extent = usage.getExtent();
		assertNotNull(extent);
		assertEquals("Netherlands offshore.", extent.getAreaDescription());
		TemporalExtent temporalExtent = extent.getTemporalExtent();
		assertNotNull(temporalExtent);
		assertEquals("1976-01", temporalExtent.getStart());
		assertTrue(temporalExtent.hasStartDateTime());
		assertEquals("1976-01", temporalExtent.getStartDateTime().toString());
		assertEquals("2001-04", temporalExtent.getEnd());
		assertTrue(temporalExtent.hasEndDateTime());
		assertEquals("2001-04", temporalExtent.getEndDateTime().toString());
		reader.close();
		assertEquals(text, usage.toString());

	}

	/**
	 * Test usages
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testUsages() throws IOException {

		String text = "USAGE[SCOPE[\"Small scale topographic mapping.\"],"
				+ "AREA[\"Finland - onshore and offshore.\"]],"
				+ "USAGE[SCOPE[\"Cadastre.\"],"
				+ "AREA[\"Finland - onshore between 26°30'E and 27°30'E.\"],"
				+ "BBOX[60.36,26.5,70.05,27.5]]";
		CRSReader reader = new CRSReader(text);
		List<Usage> usages = reader.readUsages();
		assertNotNull(usages);
		assertEquals(2, usages.size());
		Usage usage = usages.get(0);
		assertEquals("Small scale topographic mapping.", usage.getScope());
		Extent extent = usage.getExtent();
		assertNotNull(extent);
		assertEquals("Finland - onshore and offshore.",
				extent.getAreaDescription());
		usage = usages.get(1);
		assertEquals("Cadastre.", usage.getScope());
		extent = usage.getExtent();
		assertNotNull(extent);
		assertEquals("Finland - onshore between 26°30'E and 27°30'E.",
				extent.getAreaDescription());
		GeographicBoundingBox boundingBox = extent.getGeographicBoundingBox();
		assertNotNull(boundingBox);
		assertEquals(60.36, boundingBox.getLowerLeftLatitude(), 0);
		assertEquals(26.5, boundingBox.getLowerLeftLongitude(), 0);
		assertEquals(70.05, boundingBox.getUpperRightLatitude(), 0);
		assertEquals(27.5, boundingBox.getUpperRightLongitude(), 0);
		reader.close();
		CRSWriter writer = new CRSWriter();
		writer.writeUsages(usages);
		assertEquals(text, writer.toString());
		writer.close();

	}

	/**
	 * Test identifier
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testIdentifier() throws IOException {

		String text = "ID[\"Authority name\",\"Abcd_Ef\",7.1]";
		CRSReader reader = new CRSReader(text);
		Identifier identifier = reader.readIdentifier();
		assertNotNull(identifier);
		assertEquals("Authority name", identifier.getName());
		assertEquals("Abcd_Ef", identifier.getUniqueIdentifier());
		assertEquals("7.1", identifier.getVersion());
		reader.close();
		assertEquals(text, identifier.toString());

		text = "ID[\"EPSG\",4326]";
		reader = new CRSReader(text);
		identifier = reader.readIdentifier();
		assertNotNull(identifier);
		assertEquals("EPSG", identifier.getName());
		assertEquals("4326", identifier.getUniqueIdentifier());
		reader.close();
		assertEquals(text, identifier.toString());

		text = "ID[\"EPSG\",4326,URI[\"urn:ogc:def:crs:EPSG::4326\"]]";
		reader = new CRSReader(text);
		identifier = reader.readIdentifier();
		assertNotNull(identifier);
		assertEquals("EPSG", identifier.getName());
		assertEquals("4326", identifier.getUniqueIdentifier());
		assertEquals("urn:ogc:def:crs:EPSG::4326", identifier.getUri());
		reader.close();
		assertEquals(text, identifier.toString());

		text = "ID[\"EuroGeographics\",\"ES_ED50 (BAL99) to ETRS89\",\"2001-04-20\"]";
		reader = new CRSReader(text);
		identifier = reader.readIdentifier();
		assertNotNull(identifier);
		assertEquals("EuroGeographics", identifier.getName());
		assertEquals("ES_ED50 (BAL99) to ETRS89",
				identifier.getUniqueIdentifier());
		assertEquals("2001-04-20", identifier.getVersion());
		reader.close();
		assertEquals(text, identifier.toString());

	}

	/**
	 * Test remark
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testRemark() throws IOException {

		String text = "REMARK[\"A remark in ASCII\"]";
		String remark = "A remark in ASCII";
		CRSReader reader = new CRSReader(text);
		assertEquals(remark, reader.readRemark());
		reader.close();
		CRSWriter writer = new CRSWriter();
		writer.writeRemark(remark);
		assertEquals(text, writer.toString());
		writer.close();

		text = "REMARK[\"Замечание на русском языке\"]";
		remark = "Замечание на русском языке";
		reader = new CRSReader(text);
		assertEquals(remark, reader.readRemark());
		reader.close();
		writer = new CRSWriter();
		writer.writeRemark(remark);
		assertEquals(text, writer.toString());
		writer.close();

		text = "GEOGCRS[\"S-95\"," + "DATUM[\"Pulkovo 1995\","
				+ "ELLIPSOID[\"Krassowsky 1940\",6378245,298.3,"
				+ "LENGTHUNIT[\"metre\",1.0]]],CS[ellipsoidal,2],"
				+ "AXIS[\"latitude\",north,ORDER[1]],"
				+ "AXIS[\"longitude\",east,ORDER[2]],"
				+ "ANGLEUNIT[\"degree\",0.0174532925199433],"
				+ "REMARK[\"Система Геодеэических Координвт года 1995(СК-95)\"]"
				+ "]";
		String remarkText = "REMARK[\"Система Геодеэических Координвт года 1995(СК-95)\"]";
		remark = "Система Геодеэических Координвт года 1995(СК-95)";
		CoordinateReferenceSystem crs = CRSReader.read(text, true);
		assertEquals(remark, crs.getRemark());
		writer = new CRSWriter();
		writer.writeRemark(crs.getRemark());
		assertEquals(remarkText, writer.toString());
		writer.close();

	}

	/**
	 * Test length unit
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testLengthUnit() throws IOException {

		String text = "LENGTHUNIT[\"metre\",1]";
		CRSReader reader = new CRSReader(text);
		Unit unit = reader.readLengthUnit();
		reader.reset();
		assertEquals(unit, reader.readUnit());
		assertEquals(UnitType.LENGTHUNIT, unit.getType());
		assertEquals("metre", unit.getName());
		assertEquals(1, unit.getConversionFactor(), 0);
		reader.close();
		text = text.replaceAll("1", "1.0");
		assertEquals(text, unit.toString());
		unit.setType(UnitType.UNIT);
		assertEquals(text.replaceAll("LENGTHUNIT", "UNIT"), unit.toString());

		text = "LENGTHUNIT[\"German legal metre\",1.0000135965]";
		reader = new CRSReader(text);
		unit = reader.readLengthUnit();
		reader.reset();
		assertEquals(unit, reader.readUnit());
		assertEquals(UnitType.LENGTHUNIT, unit.getType());
		assertEquals("German legal metre", unit.getName());
		assertEquals(1.0000135965, unit.getConversionFactor(), 0);
		reader.close();
		assertEquals(text, unit.toString());
		unit.setType(UnitType.UNIT);
		assertEquals(text.replaceAll("LENGTHUNIT", "UNIT"), unit.toString());

	}

	/**
	 * Test angle unit
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testAngleUnit() throws IOException {

		String text = "ANGLEUNIT[\"degree\",0.0174532925199433]";
		CRSReader reader = new CRSReader(text);
		Unit unit = reader.readAngleUnit();
		reader.reset();
		assertEquals(unit, reader.readUnit());
		assertEquals(UnitType.ANGLEUNIT, unit.getType());
		assertEquals("degree", unit.getName());
		assertEquals(0.0174532925199433, unit.getConversionFactor(), 0);
		reader.close();
		assertEquals(text, unit.toString());
		unit.setType(UnitType.UNIT);
		assertEquals(text.replaceAll("ANGLEUNIT", "UNIT"), unit.toString());

	}

	/**
	 * Test scale unit
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testScaleUnit() throws IOException {

		String text = "SCALEUNIT[\"parts per million\",1E-06]";
		CRSReader reader = new CRSReader(text);
		Unit unit = reader.readScaleUnit();
		reader.reset();
		assertEquals(unit, reader.readUnit());
		assertEquals(UnitType.SCALEUNIT, unit.getType());
		assertEquals("parts per million", unit.getName());
		assertEquals(1E-06, unit.getConversionFactor(), 0);
		reader.close();
		text = text.replaceAll("1E-06", "1.0E-6");
		assertEquals(text, unit.toString());
		unit.setType(UnitType.UNIT);
		assertEquals(text.replaceAll("SCALEUNIT", "UNIT"), unit.toString());

	}

	/**
	 * Test parametric unit
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testParametricUnit() throws IOException {

		String text = "PARAMETRICUNIT[\"hectopascal\",100]";
		CRSReader reader = new CRSReader(text);
		Unit unit = reader.readParametricUnit();
		reader.reset();
		assertEquals(unit, reader.readUnit());
		assertEquals(UnitType.PARAMETRICUNIT, unit.getType());
		assertEquals("hectopascal", unit.getName());
		assertEquals(100, unit.getConversionFactor(), 0);
		reader.close();
		assertEquals(text.replaceAll("100", "100.0"), unit.toString());

	}

	/**
	 * Test time unit
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testTimeUnit() throws IOException {

		String text = "TIMEUNIT[\"millisecond\",0.001]";
		CRSReader reader = new CRSReader(text);
		Unit unit = reader.readTimeUnit();
		reader.reset();
		assertEquals(unit, reader.readUnit());
		assertEquals(UnitType.TIMEUNIT, unit.getType());
		assertEquals("millisecond", unit.getName());
		assertEquals(0.001, unit.getConversionFactor(), 0);
		reader.close();
		assertEquals(text, unit.toString());

		text = "TIMEUNIT[\"calendar month\"]";
		reader = new CRSReader(text);
		unit = reader.readTimeUnit();
		reader.reset();
		assertEquals(unit, reader.readUnit());
		assertEquals(UnitType.TIMEUNIT, unit.getType());
		assertEquals("calendar month", unit.getName());
		reader.close();
		assertEquals(text, unit.toString());

		text = "TIMEUNIT[\"calendar second\"]";
		reader = new CRSReader(text);
		unit = reader.readTimeUnit();
		reader.reset();
		assertEquals(unit, reader.readUnit());
		assertEquals(UnitType.TIMEUNIT, unit.getType());
		assertEquals("calendar second", unit.getName());
		reader.close();
		assertEquals(text, unit.toString());

		text = "TIMEUNIT[\"day\",86400.0]";
		reader = new CRSReader(text);
		unit = reader.readTimeUnit();
		reader.reset();
		assertEquals(unit, reader.readUnit());
		assertEquals(UnitType.TIMEUNIT, unit.getType());
		assertEquals("day", unit.getName());
		assertEquals(86400.0, unit.getConversionFactor(), 0);
		reader.close();
		assertEquals(text, unit.toString());

	}

	/**
	 * Test geodetic coordinate system
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testGeodeticCoordinateSystem() throws IOException {

		String text = "CS[Cartesian,3],AXIS[\"(X)\",geocentricX],"
				+ "AXIS[\"(Y)\",geocentricY],AXIS[\"(Z)\",geocentricZ],"
				+ "LENGTHUNIT[\"metre\",1.0]";
		CRSReader reader = new CRSReader(text);
		CoordinateSystem coordinateSystem = reader.readCoordinateSystem();
		assertEquals(CoordinateSystemType.CARTESIAN,
				coordinateSystem.getType());
		assertEquals(3, coordinateSystem.getDimension());
		List<Axis> axes = coordinateSystem.getAxes();
		assertEquals(3, axes.size());
		assertEquals("X", axes.get(0).getAbbreviation());
		assertEquals(AxisDirectionType.GEOCENTRIC_X,
				axes.get(0).getDirection());
		assertEquals("Y", axes.get(1).getAbbreviation());
		assertEquals(AxisDirectionType.GEOCENTRIC_Y,
				axes.get(1).getDirection());
		assertEquals("Z", axes.get(2).getAbbreviation());
		assertEquals(AxisDirectionType.GEOCENTRIC_Z,
				axes.get(2).getDirection());
		Unit unit = coordinateSystem.getUnit();
		assertEquals(UnitType.LENGTHUNIT, unit.getType());
		assertEquals("metre", unit.getName());
		assertEquals(1.0, unit.getConversionFactor(), 0);
		reader.close();
		assertEquals(text, coordinateSystem.toString());

		text = "CS[Cartesian,3],"
				+ "AXIS[\"(X)\",east],AXIS[\"(Y)\",north],AXIS[\"(Z)\",up],"
				+ "LENGTHUNIT[\"metre\",1.0]";
		reader = new CRSReader(text);
		coordinateSystem = reader.readCoordinateSystem();
		assertEquals(CoordinateSystemType.CARTESIAN,
				coordinateSystem.getType());
		assertEquals(3, coordinateSystem.getDimension());
		axes = coordinateSystem.getAxes();
		assertEquals(3, axes.size());
		assertEquals("X", axes.get(0).getAbbreviation());
		assertEquals(AxisDirectionType.EAST, axes.get(0).getDirection());
		assertEquals("Y", axes.get(1).getAbbreviation());
		assertEquals(AxisDirectionType.NORTH, axes.get(1).getDirection());
		assertEquals("Z", axes.get(2).getAbbreviation());
		assertEquals(AxisDirectionType.UP, axes.get(2).getDirection());
		unit = coordinateSystem.getUnit();
		assertEquals(UnitType.LENGTHUNIT, unit.getType());
		assertEquals("metre", unit.getName());
		assertEquals(1.0, unit.getConversionFactor(), 0);
		reader.close();
		assertEquals(text, coordinateSystem.toString());

		text = "CS[spherical,3],"
				+ "AXIS[\"distance (r)\",awayFrom,ORDER[1],LENGTHUNIT[\"kilometre\",1000]],"
				+ "AXIS[\"longitude (U)\",counterClockwise,BEARING[0],ORDER[2],"
				+ "ANGLEUNIT[\"degree\",0.0174532925199433]],"
				+ "AXIS[\"elevation (V)\",up,ORDER[3],"
				+ "ANGLEUNIT[\"degree\",0.0174532925199433]]";
		reader = new CRSReader(text);
		coordinateSystem = reader.readCoordinateSystem();
		assertEquals(CoordinateSystemType.SPHERICAL,
				coordinateSystem.getType());
		assertEquals(3, coordinateSystem.getDimension());
		axes = coordinateSystem.getAxes();
		assertEquals(3, axes.size());
		assertEquals("distance", axes.get(0).getName());
		assertEquals("r", axes.get(0).getAbbreviation());
		assertEquals(AxisDirectionType.AWAY_FROM, axes.get(0).getDirection());
		assertEquals(1, axes.get(0).getOrder().intValue());
		assertEquals(UnitType.LENGTHUNIT, axes.get(0).getUnit().getType());
		assertEquals("kilometre", axes.get(0).getUnit().getName());
		assertEquals(1000, axes.get(0).getUnit().getConversionFactor(), 0);
		assertEquals("longitude", axes.get(1).getName());
		assertEquals("U", axes.get(1).getAbbreviation());
		assertEquals(AxisDirectionType.COUNTER_CLOCKWISE,
				axes.get(1).getDirection());
		assertEquals(2, axes.get(1).getOrder().intValue());
		assertEquals(UnitType.ANGLEUNIT, axes.get(1).getUnit().getType());
		assertEquals("degree", axes.get(1).getUnit().getName());
		assertEquals(0.0174532925199433,
				axes.get(1).getUnit().getConversionFactor(), 0);
		assertEquals("elevation", axes.get(2).getName());
		assertEquals("V", axes.get(2).getAbbreviation());
		assertEquals(AxisDirectionType.UP, axes.get(2).getDirection());
		assertEquals(3, axes.get(2).getOrder().intValue());
		assertEquals(UnitType.ANGLEUNIT, axes.get(2).getUnit().getType());
		assertEquals("degree", axes.get(2).getUnit().getName());
		assertEquals(0.0174532925199433,
				axes.get(2).getUnit().getConversionFactor(), 0);
		reader.close();
		assertEquals(text.replaceAll("0]", "0.0]"),
				coordinateSystem.toString());

	}

	/**
	 * Test geographic coordinate system
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testGeographicCoordinateSystem() throws IOException {

		String text = "CS[ellipsoidal,3],"
				+ "AXIS[\"latitude\",north,ORDER[1],ANGLEUNIT[\"degree\",0.0174532925199433]],"
				+ "AXIS[\"longitude\",east,ORDER[2],ANGLEUNIT[\"degree\",0.0174532925199433]],"
				+ "AXIS[\"ellipsoidal height (h)\",up,ORDER[3],LENGTHUNIT[\"metre\",1.0]]";
		CRSReader reader = new CRSReader(text);
		CoordinateSystem coordinateSystem = reader.readCoordinateSystem();
		assertEquals(CoordinateSystemType.ELLIPSOIDAL,
				coordinateSystem.getType());
		assertEquals(3, coordinateSystem.getDimension());
		List<Axis> axes = coordinateSystem.getAxes();
		assertEquals(3, axes.size());
		assertEquals("latitude", axes.get(0).getName());
		assertEquals(AxisDirectionType.NORTH, axes.get(0).getDirection());
		assertEquals(1, axes.get(0).getOrder().intValue());
		assertEquals(UnitType.ANGLEUNIT, axes.get(0).getUnit().getType());
		assertEquals("degree", axes.get(0).getUnit().getName());
		assertEquals(0.0174532925199433,
				axes.get(0).getUnit().getConversionFactor(), 0);
		assertEquals("longitude", axes.get(1).getName());
		assertEquals(AxisDirectionType.EAST, axes.get(1).getDirection());
		assertEquals(2, axes.get(1).getOrder().intValue());
		assertEquals(UnitType.ANGLEUNIT, axes.get(1).getUnit().getType());
		assertEquals("degree", axes.get(1).getUnit().getName());
		assertEquals(0.0174532925199433,
				axes.get(1).getUnit().getConversionFactor(), 0);
		assertEquals("ellipsoidal height", axes.get(2).getName());
		assertEquals("h", axes.get(2).getAbbreviation());
		assertEquals(AxisDirectionType.UP, axes.get(2).getDirection());
		assertEquals(3, axes.get(2).getOrder().intValue());
		assertEquals(UnitType.LENGTHUNIT, axes.get(2).getUnit().getType());
		assertEquals("metre", axes.get(2).getUnit().getName());
		assertEquals(1.0, axes.get(2).getUnit().getConversionFactor(), 0);
		reader.close();
		assertEquals(text, coordinateSystem.toString());

		text = "CS[ellipsoidal,2],AXIS[\"(lat)\",north],"
				+ "AXIS[\"(lon)\",east],"
				+ "ANGLEUNIT[\"degree\",0.0174532925199433]";
		reader = new CRSReader(text);
		coordinateSystem = reader.readCoordinateSystem();
		assertEquals(CoordinateSystemType.ELLIPSOIDAL,
				coordinateSystem.getType());
		assertEquals(2, coordinateSystem.getDimension());
		axes = coordinateSystem.getAxes();
		assertEquals(2, axes.size());
		assertEquals("lat", axes.get(0).getAbbreviation());
		assertEquals(AxisDirectionType.NORTH, axes.get(0).getDirection());
		assertEquals("lon", axes.get(1).getAbbreviation());
		assertEquals(AxisDirectionType.EAST, axes.get(1).getDirection());
		Unit unit = coordinateSystem.getUnit();
		assertEquals(UnitType.ANGLEUNIT, unit.getType());
		assertEquals("degree", unit.getName());
		assertEquals(0.0174532925199433, unit.getConversionFactor(), 0);
		reader.close();
		assertEquals(text, coordinateSystem.toString());

	}

	/**
	 * Test projected coordinate system
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testProjectedCoordinateSystem() throws IOException {

		String text = "CS[Cartesian,2],"
				+ "AXIS[\"(E)\",east,ORDER[1],LENGTHUNIT[\"metre\",1.0]],"
				+ "AXIS[\"(N)\",north,ORDER[2],LENGTHUNIT[\"metre\",1.0]]";
		CRSReader reader = new CRSReader(text);
		CoordinateSystem coordinateSystem = reader.readCoordinateSystem();
		assertEquals(CoordinateSystemType.CARTESIAN,
				coordinateSystem.getType());
		assertEquals(2, coordinateSystem.getDimension());
		List<Axis> axes = coordinateSystem.getAxes();
		assertEquals(2, axes.size());
		assertEquals("E", axes.get(0).getAbbreviation());
		assertEquals(AxisDirectionType.EAST, axes.get(0).getDirection());
		assertEquals(1, axes.get(0).getOrder().intValue());
		assertEquals(UnitType.LENGTHUNIT, axes.get(0).getUnit().getType());
		assertEquals("metre", axes.get(0).getUnit().getName());
		assertEquals(1.0, axes.get(0).getUnit().getConversionFactor(), 0);
		assertEquals("N", axes.get(1).getAbbreviation());
		assertEquals(AxisDirectionType.NORTH, axes.get(1).getDirection());
		assertEquals(2, axes.get(1).getOrder().intValue());
		assertEquals(UnitType.LENGTHUNIT, axes.get(1).getUnit().getType());
		assertEquals("metre", axes.get(1).getUnit().getName());
		assertEquals(1.0, axes.get(1).getUnit().getConversionFactor(), 0);
		reader.close();
		assertEquals(text, coordinateSystem.toString());

		text = "CS[Cartesian,2],AXIS[\"(E)\",east],"
				+ "AXIS[\"(N)\",north],LENGTHUNIT[\"metre\",1.0]";
		reader = new CRSReader(text);
		coordinateSystem = reader.readCoordinateSystem();
		assertEquals(CoordinateSystemType.CARTESIAN,
				coordinateSystem.getType());
		assertEquals(2, coordinateSystem.getDimension());
		axes = coordinateSystem.getAxes();
		assertEquals(2, axes.size());
		assertEquals("E", axes.get(0).getAbbreviation());
		assertEquals(AxisDirectionType.EAST, axes.get(0).getDirection());
		assertEquals("N", axes.get(1).getAbbreviation());
		assertEquals(AxisDirectionType.NORTH, axes.get(1).getDirection());
		Unit unit = coordinateSystem.getUnit();
		assertEquals(UnitType.LENGTHUNIT, unit.getType());
		assertEquals("metre", unit.getName());
		assertEquals(1.0, unit.getConversionFactor(), 0);
		reader.close();
		assertEquals(text, coordinateSystem.toString());

		text = "CS[Cartesian,2],AXIS[\"northing (X)\",north,ORDER[1]],"
				+ "AXIS[\"easting (Y)\",east,ORDER[2]],"
				+ "LENGTHUNIT[\"German legal metre\",1.0000135965]";
		reader = new CRSReader(text);
		coordinateSystem = reader.readCoordinateSystem();
		assertEquals(CoordinateSystemType.CARTESIAN,
				coordinateSystem.getType());
		assertEquals(2, coordinateSystem.getDimension());
		axes = coordinateSystem.getAxes();
		assertEquals(2, axes.size());
		assertEquals("northing", axes.get(0).getName());
		assertEquals("X", axes.get(0).getAbbreviation());
		assertEquals(AxisDirectionType.NORTH, axes.get(0).getDirection());
		assertEquals(1, axes.get(0).getOrder().intValue());
		assertEquals("easting", axes.get(1).getName());
		assertEquals("Y", axes.get(1).getAbbreviation());
		assertEquals(AxisDirectionType.EAST, axes.get(1).getDirection());
		assertEquals(2, axes.get(1).getOrder().intValue());
		unit = coordinateSystem.getUnit();
		assertEquals(UnitType.LENGTHUNIT, unit.getType());
		assertEquals("German legal metre", unit.getName());
		assertEquals(1.0000135965, unit.getConversionFactor(), 0);
		reader.close();
		assertEquals(text, coordinateSystem.toString());

		text = "CS[Cartesian,2]," + "AXIS[\"easting (X)\",south,"
				+ "MERIDIAN[90,ANGLEUNIT[\"degree\",0.0174532925199433]],ORDER[1]"
				+ "],AXIS[\"northing (Y)\",south,"
				+ "MERIDIAN[180,ANGLEUNIT[\"degree\",0.0174532925199433]],ORDER[2]"
				+ "],LENGTHUNIT[\"metre\",1.0]";
		reader = new CRSReader(text);
		coordinateSystem = reader.readCoordinateSystem();
		assertEquals(CoordinateSystemType.CARTESIAN,
				coordinateSystem.getType());
		assertEquals(2, coordinateSystem.getDimension());
		axes = coordinateSystem.getAxes();
		assertEquals(2, axes.size());
		assertEquals("easting", axes.get(0).getName());
		assertEquals("X", axes.get(0).getAbbreviation());
		assertEquals(AxisDirectionType.SOUTH, axes.get(0).getDirection());
		assertEquals(90, axes.get(0).getMeridian(), 0);
		assertEquals(UnitType.ANGLEUNIT,
				axes.get(0).getMeridianUnit().getType());
		assertEquals("degree", axes.get(0).getMeridianUnit().getName());
		assertEquals(0.0174532925199433,
				axes.get(0).getMeridianUnit().getConversionFactor(), 0);
		assertEquals(1, axes.get(0).getOrder().intValue());
		assertEquals("northing", axes.get(1).getName());
		assertEquals("Y", axes.get(1).getAbbreviation());
		assertEquals(AxisDirectionType.SOUTH, axes.get(1).getDirection());
		assertEquals(180, axes.get(1).getMeridian(), 0);
		assertEquals(UnitType.ANGLEUNIT,
				axes.get(1).getMeridianUnit().getType());
		assertEquals("degree", axes.get(1).getMeridianUnit().getName());
		assertEquals(0.0174532925199433,
				axes.get(1).getMeridianUnit().getConversionFactor(), 0);
		assertEquals(2, axes.get(1).getOrder().intValue());
		unit = coordinateSystem.getUnit();
		assertEquals(UnitType.LENGTHUNIT, unit.getType());
		assertEquals("metre", unit.getName());
		assertEquals(1.0, unit.getConversionFactor(), 0);
		reader.close();
		assertEquals(text.replaceAll(",ANGLEUNIT", ".0,ANGLEUNIT"),
				coordinateSystem.toString());

		text = "CS[Cartesian,3],AXIS[\"(E)\",east],"
				+ "AXIS[\"(N)\",north],AXIS[\"ellipsoid height (h)\",up],"
				+ "LENGTHUNIT[\"metre\",1.0]";
		reader = new CRSReader(text);
		coordinateSystem = reader.readCoordinateSystem();
		assertEquals(CoordinateSystemType.CARTESIAN,
				coordinateSystem.getType());
		assertEquals(3, coordinateSystem.getDimension());
		axes = coordinateSystem.getAxes();
		assertEquals(3, axes.size());
		assertEquals("E", axes.get(0).getAbbreviation());
		assertEquals(AxisDirectionType.EAST, axes.get(0).getDirection());
		assertEquals("N", axes.get(1).getAbbreviation());
		assertEquals(AxisDirectionType.NORTH, axes.get(1).getDirection());
		assertEquals("ellipsoid height", axes.get(2).getName());
		assertEquals("h", axes.get(2).getAbbreviation());
		assertEquals(AxisDirectionType.UP, axes.get(2).getDirection());
		unit = coordinateSystem.getUnit();
		assertEquals(UnitType.LENGTHUNIT, unit.getType());
		assertEquals("metre", unit.getName());
		assertEquals(1.0, unit.getConversionFactor(), 0);
		reader.close();
		assertEquals(text, coordinateSystem.toString());

	}

	/**
	 * Test vertical coordinate system
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testVerticalCoordinateSystem() throws IOException {

		String text = "CS[vertical,1],AXIS[\"gravity-related height (H)\",up],"
				+ "LENGTHUNIT[\"metre\",1.0]";
		CRSReader reader = new CRSReader(text);
		CoordinateSystem coordinateSystem = reader.readCoordinateSystem();
		assertEquals(CoordinateSystemType.VERTICAL, coordinateSystem.getType());
		assertEquals(1, coordinateSystem.getDimension());
		List<Axis> axes = coordinateSystem.getAxes();
		assertEquals(1, axes.size());
		assertEquals("gravity-related height", axes.get(0).getName());
		assertEquals("H", axes.get(0).getAbbreviation());
		assertEquals(AxisDirectionType.UP, axes.get(0).getDirection());
		Unit unit = coordinateSystem.getUnit();
		assertEquals(UnitType.LENGTHUNIT, unit.getType());
		assertEquals("metre", unit.getName());
		assertEquals(1.0, unit.getConversionFactor(), 0);
		reader.close();
		assertEquals(text, coordinateSystem.toString());

		text = "CS[vertical,1],AXIS[\"depth (D)\",down,"
				+ "LENGTHUNIT[\"metre\",1.0]]";
		reader = new CRSReader(text);
		coordinateSystem = reader.readCoordinateSystem();
		assertEquals(CoordinateSystemType.VERTICAL, coordinateSystem.getType());
		assertEquals(1, coordinateSystem.getDimension());
		axes = coordinateSystem.getAxes();
		assertEquals(1, axes.size());
		assertEquals("depth", axes.get(0).getName());
		assertEquals("D", axes.get(0).getAbbreviation());
		assertEquals(AxisDirectionType.DOWN, axes.get(0).getDirection());
		assertEquals(UnitType.LENGTHUNIT, axes.get(0).getUnit().getType());
		assertEquals("metre", axes.get(0).getUnit().getName());
		assertEquals(1.0, axes.get(0).getUnit().getConversionFactor(), 0);
		reader.close();
		assertEquals(text, coordinateSystem.toString());

	}

	/**
	 * Test engineering coordinate system
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testEngineeringCoordinateSystem() throws IOException {

		String text = "CS[Cartesian,2],"
				+ "AXIS[\"site north (x)\",southeast,ORDER[1]],"
				+ "AXIS[\"site east (y)\",southwest,ORDER[2]],"
				+ "LENGTHUNIT[\"metre\",1.0]";
		CRSReader reader = new CRSReader(text);
		CoordinateSystem coordinateSystem = reader.readCoordinateSystem();
		assertEquals(CoordinateSystemType.CARTESIAN,
				coordinateSystem.getType());
		assertEquals(2, coordinateSystem.getDimension());
		List<Axis> axes = coordinateSystem.getAxes();
		assertEquals(2, axes.size());
		assertEquals("site north", axes.get(0).getName());
		assertEquals("x", axes.get(0).getAbbreviation());
		assertEquals(AxisDirectionType.SOUTH_EAST, axes.get(0).getDirection());
		assertEquals(1, axes.get(0).getOrder().intValue());
		assertEquals("site east", axes.get(1).getName());
		assertEquals("y", axes.get(1).getAbbreviation());
		assertEquals(AxisDirectionType.SOUTH_WEST, axes.get(1).getDirection());
		assertEquals(2, axes.get(1).getOrder().intValue());
		Unit unit = coordinateSystem.getUnit();
		assertEquals(UnitType.LENGTHUNIT, unit.getType());
		assertEquals("metre", unit.getName());
		assertEquals(1.0, unit.getConversionFactor(), 0);
		reader.close();
		assertEquals(text.replaceAll("southeast", "southEast").replaceAll(
				"southwest", "southWest"), coordinateSystem.toString());

		text = "CS[polar,2],"
				+ "AXIS[\"distance (r)\",awayFrom,ORDER[1],LENGTHUNIT[\"metre\",1.0]],"
				+ "AXIS[\"bearing (U)\",clockwise,BEARING[234],ORDER[2],"
				+ "ANGLEUNIT[\"degree\",0.0174532925199433]]";
		reader = new CRSReader(text);
		coordinateSystem = reader.readCoordinateSystem();
		assertEquals(CoordinateSystemType.POLAR, coordinateSystem.getType());
		assertEquals(2, coordinateSystem.getDimension());
		axes = coordinateSystem.getAxes();
		assertEquals(2, axes.size());
		assertEquals("distance", axes.get(0).getName());
		assertEquals("r", axes.get(0).getAbbreviation());
		assertEquals(AxisDirectionType.AWAY_FROM, axes.get(0).getDirection());
		assertEquals(1, axes.get(0).getOrder().intValue());
		assertEquals(UnitType.LENGTHUNIT, axes.get(0).getUnit().getType());
		assertEquals("metre", axes.get(0).getUnit().getName());
		assertEquals(1.0, axes.get(0).getUnit().getConversionFactor(), 0);
		assertEquals("bearing", axes.get(1).getName());
		assertEquals("U", axes.get(1).getAbbreviation());
		assertEquals(AxisDirectionType.CLOCKWISE, axes.get(1).getDirection());
		assertEquals(234, axes.get(1).getBearing(), 0);
		assertEquals(2, axes.get(1).getOrder().intValue());
		assertEquals(UnitType.ANGLEUNIT, axes.get(1).getUnit().getType());
		assertEquals("degree", axes.get(1).getUnit().getName());
		assertEquals(0.0174532925199433,
				axes.get(1).getUnit().getConversionFactor(), 0);
		reader.close();
		assertEquals(text.replaceAll("234]", "234.0]"),
				coordinateSystem.toString());

		text = "CS[Cartesian,3],AXIS[\"ahead (x)\",forward,ORDER[1]],"
				+ "AXIS[\"right (y)\",starboard,ORDER[2]],"
				+ "AXIS[\"down (z)\",down,ORDER[3]],"
				+ "LENGTHUNIT[\"metre\",1.0]";
		reader = new CRSReader(text);
		coordinateSystem = reader.readCoordinateSystem();
		assertEquals(CoordinateSystemType.CARTESIAN,
				coordinateSystem.getType());
		assertEquals(3, coordinateSystem.getDimension());
		axes = coordinateSystem.getAxes();
		assertEquals(3, axes.size());
		assertEquals("ahead", axes.get(0).getName());
		assertEquals("x", axes.get(0).getAbbreviation());
		assertEquals(AxisDirectionType.FORWARD, axes.get(0).getDirection());
		assertEquals(1, axes.get(0).getOrder().intValue());
		assertEquals("right", axes.get(1).getName());
		assertEquals("y", axes.get(1).getAbbreviation());
		assertEquals(AxisDirectionType.STARBOARD, axes.get(1).getDirection());
		assertEquals(2, axes.get(1).getOrder().intValue());
		assertEquals("down", axes.get(2).getName());
		assertEquals("z", axes.get(2).getAbbreviation());
		assertEquals(AxisDirectionType.DOWN, axes.get(2).getDirection());
		assertEquals(3, axes.get(2).getOrder().intValue());
		unit = coordinateSystem.getUnit();
		assertEquals(UnitType.LENGTHUNIT, unit.getType());
		assertEquals("metre", unit.getName());
		assertEquals(1.0, unit.getConversionFactor(), 0);
		reader.close();
		assertEquals(text, coordinateSystem.toString());

		text = "CS[ordinal,2],AXIS[\"Inline (I)\",northEast,ORDER[1]],"
				+ "AXIS[\"Crossline (J)\",northwest,ORDER[2]]";
		reader = new CRSReader(text);
		coordinateSystem = reader.readCoordinateSystem();
		assertEquals(CoordinateSystemType.ORDINAL, coordinateSystem.getType());
		assertEquals(2, coordinateSystem.getDimension());
		axes = coordinateSystem.getAxes();
		assertEquals(2, axes.size());
		assertEquals("Inline", axes.get(0).getName());
		assertEquals("I", axes.get(0).getAbbreviation());
		assertEquals(AxisDirectionType.NORTH_EAST, axes.get(0).getDirection());
		assertEquals(1, axes.get(0).getOrder().intValue());
		assertEquals("Crossline", axes.get(1).getName());
		assertEquals("J", axes.get(1).getAbbreviation());
		assertEquals(AxisDirectionType.NORTH_WEST, axes.get(1).getDirection());
		assertEquals(2, axes.get(1).getOrder().intValue());
		reader.close();
		assertEquals(text.replaceAll("northwest", "northWest"),
				coordinateSystem.toString());

	}

	/**
	 * Test geodetic datum ensemble
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testGeodeticDatumEnsemble() throws IOException {

		String text = "ENSEMBLE[\"WGS 84 ensemble\","
				+ "MEMBER[\"WGS 84 (TRANSIT)\"],MEMBER[\"WGS 84 (G730)\"],"
				+ "MEMBER[\"WGS 84 (G834)\"],MEMBER[\"WGS 84 (G1150)\"],"
				+ "MEMBER[\"WGS 84 (G1674)\"],MEMBER[\"WGS 84 (G1762)\"],"
				+ "ELLIPSOID[\"WGS 84\",6378137,298.2572236,LENGTHUNIT[\"metre\",1.0]],"
				+ "ENSEMBLEACCURACY[2]]";
		CRSReader reader = new CRSReader(text);
		GeodeticDatumEnsemble datumEnsemble = reader
				.readGeodeticDatumEnsemble();
		assertNotNull(datumEnsemble);
		assertEquals("WGS 84 ensemble", datumEnsemble.getName());
		assertEquals(6, datumEnsemble.getMembers().size());
		assertEquals("WGS 84 (TRANSIT)",
				datumEnsemble.getMembers().get(0).getName());
		assertEquals("WGS 84 (G730)",
				datumEnsemble.getMembers().get(1).getName());
		assertEquals("WGS 84 (G834)",
				datumEnsemble.getMembers().get(2).getName());
		assertEquals("WGS 84 (G1150)",
				datumEnsemble.getMembers().get(3).getName());
		assertEquals("WGS 84 (G1674)",
				datumEnsemble.getMembers().get(4).getName());
		assertEquals("WGS 84 (G1762)",
				datumEnsemble.getMembers().get(5).getName());
		assertEquals("WGS 84", datumEnsemble.getEllipsoid().getName());
		assertEquals(6378137, datumEnsemble.getEllipsoid().getSemiMajorAxis(),
				0);
		assertEquals(298.2572236,
				datumEnsemble.getEllipsoid().getInverseFlattening(), 0);
		assertEquals(UnitType.LENGTHUNIT,
				datumEnsemble.getEllipsoid().getUnit().getType());
		assertEquals("metre", datumEnsemble.getEllipsoid().getUnit().getName());
		assertEquals(1.0,
				datumEnsemble.getEllipsoid().getUnit().getConversionFactor(),
				0);
		assertEquals(2, datumEnsemble.getAccuracy(), 0);
		reader.close();
		assertEquals(
				text.replaceAll("6378137", "6378137.0").replace("[2]", "[2.0]"),
				datumEnsemble.toString());

		text = "ENSEMBLE[\"WGS 84 ensemble\","
				+ "MEMBER[\"WGS 84 (TRANSIT)\",ID[\"EPSG\",1166]],"
				+ "MEMBER[\"WGS 84 (G730)\",ID[\"EPSG\",1152]],"
				+ "MEMBER[\"WGS 84 (G834)\",ID[\"EPSG\",1153]],"
				+ "MEMBER[\"WGS 84 (G1150)\",ID[\"EPSG\",1154]],"
				+ "MEMBER[\"WGS 84 (G1674)\",ID[\"EPSG\",1155]],"
				+ "MEMBER[\"WGS 84 (G1762)\",ID[\"EPSG\",1156]],"
				+ "ELLIPSOID[\"WGS 84\",6378137,298.2572236,LENGTHUNIT[\"metre\",1.0]],"
				+ "ENSEMBLEACCURACY[2]]";
		reader = new CRSReader(text);
		datumEnsemble = reader.readGeodeticDatumEnsemble();
		assertNotNull(datumEnsemble);
		assertEquals("WGS 84 ensemble", datumEnsemble.getName());
		assertEquals(6, datumEnsemble.getMembers().size());
		assertEquals("WGS 84 (TRANSIT)",
				datumEnsemble.getMembers().get(0).getName());
		assertEquals("EPSG", datumEnsemble.getMembers().get(0).getIdentifiers()
				.get(0).getName());
		assertEquals("1166", datumEnsemble.getMembers().get(0).getIdentifiers()
				.get(0).getUniqueIdentifier());
		assertEquals("WGS 84 (G730)",
				datumEnsemble.getMembers().get(1).getName());
		assertEquals("EPSG", datumEnsemble.getMembers().get(1).getIdentifiers()
				.get(0).getName());
		assertEquals("1152", datumEnsemble.getMembers().get(1).getIdentifiers()
				.get(0).getUniqueIdentifier());
		assertEquals("WGS 84 (G834)",
				datumEnsemble.getMembers().get(2).getName());
		assertEquals("EPSG", datumEnsemble.getMembers().get(2).getIdentifiers()
				.get(0).getName());
		assertEquals("1153", datumEnsemble.getMembers().get(2).getIdentifiers()
				.get(0).getUniqueIdentifier());
		assertEquals("WGS 84 (G1150)",
				datumEnsemble.getMembers().get(3).getName());
		assertEquals("EPSG", datumEnsemble.getMembers().get(3).getIdentifiers()
				.get(0).getName());
		assertEquals("1154", datumEnsemble.getMembers().get(3).getIdentifiers()
				.get(0).getUniqueIdentifier());
		assertEquals("WGS 84 (G1674)",
				datumEnsemble.getMembers().get(4).getName());
		assertEquals("EPSG", datumEnsemble.getMembers().get(4).getIdentifiers()
				.get(0).getName());
		assertEquals("1155", datumEnsemble.getMembers().get(4).getIdentifiers()
				.get(0).getUniqueIdentifier());
		assertEquals("WGS 84 (G1762)",
				datumEnsemble.getMembers().get(5).getName());
		assertEquals("EPSG", datumEnsemble.getMembers().get(5).getIdentifiers()
				.get(0).getName());
		assertEquals("1156", datumEnsemble.getMembers().get(5).getIdentifiers()
				.get(0).getUniqueIdentifier());
		assertEquals("WGS 84", datumEnsemble.getEllipsoid().getName());
		assertEquals(6378137, datumEnsemble.getEllipsoid().getSemiMajorAxis(),
				0);
		assertEquals(298.2572236,
				datumEnsemble.getEllipsoid().getInverseFlattening(), 0);
		assertEquals(UnitType.LENGTHUNIT,
				datumEnsemble.getEllipsoid().getUnit().getType());
		assertEquals("metre", datumEnsemble.getEllipsoid().getUnit().getName());
		assertEquals(1.0,
				datumEnsemble.getEllipsoid().getUnit().getConversionFactor(),
				0);
		assertEquals(2, datumEnsemble.getAccuracy(), 0);
		reader.close();
		assertEquals(
				text.replaceAll("6378137", "6378137.0").replace("[2]", "[2.0]"),
				datumEnsemble.toString());

	}

	/**
	 * Test vertical datum ensemble
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testVerticalDatumEnsemble() throws IOException {

		String text = "ENSEMBLE[\"EVRS ensemble\","
				+ "MEMBER[\"EVRF2000\"],MEMBER[\"EVRF2007\"],"
				+ "ENSEMBLEACCURACY[0.01]]";
		CRSReader reader = new CRSReader(text);
		DatumEnsemble datumEnsemble = reader.readVerticalDatumEnsemble();
		assertNotNull(datumEnsemble);
		assertEquals("EVRS ensemble", datumEnsemble.getName());
		assertEquals(2, datumEnsemble.getMembers().size());
		assertEquals("EVRF2000", datumEnsemble.getMembers().get(0).getName());
		assertEquals("EVRF2007", datumEnsemble.getMembers().get(1).getName());
		assertEquals(0.01, datumEnsemble.getAccuracy(), 0);
		reader.close();
		assertEquals(text, datumEnsemble.toString());

	}

	/**
	 * Test dynamic
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testDynamic() throws IOException {

		String text = "DYNAMIC[FRAMEEPOCH[2010.0]]";
		CRSReader reader = new CRSReader(text);
		Dynamic dynamic = reader.readDynamic();
		assertEquals(2010.0, dynamic.getReferenceEpoch(), 0);
		reader.close();
		assertEquals(text, dynamic.toString());

		text = "DYNAMIC[FRAMEEPOCH[2010.0],MODEL[\"NAD83(CSRS)v6 velocity grid\"]]";
		reader = new CRSReader(text);
		dynamic = reader.readDynamic();
		assertEquals(2010.0, dynamic.getReferenceEpoch(), 0);
		assertEquals("NAD83(CSRS)v6 velocity grid",
				dynamic.getDeformationModelName());
		reader.close();
		assertEquals(text, dynamic.toString());

	}

	/**
	 * Test ellipsoid
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testEllipsoid() throws IOException {

		String text = "ELLIPSOID[\"GRS 1980\",6378137,298.257222101,LENGTHUNIT[\"metre\",1.0]]";
		CRSReader reader = new CRSReader(text);
		Ellipsoid ellipsoid = reader.readEllipsoid();
		assertEquals("GRS 1980", ellipsoid.getName());
		assertEquals(6378137, ellipsoid.getSemiMajorAxis(), 0);
		assertEquals(298.257222101, ellipsoid.getInverseFlattening(), 0);
		assertEquals(UnitType.LENGTHUNIT, ellipsoid.getUnit().getType());
		assertEquals("metre", ellipsoid.getUnit().getName());
		assertEquals(1.0, ellipsoid.getUnit().getConversionFactor(), 0);
		reader.close();
		assertEquals(text.replaceAll("6378137", "6378137.0"),
				ellipsoid.toString());

		text = "SPHEROID[\"GRS 1980\",6378137.0,298.257222101]";
		reader = new CRSReader(text);
		ellipsoid = reader.readEllipsoid();
		assertEquals("GRS 1980", ellipsoid.getName());
		assertEquals(6378137, ellipsoid.getSemiMajorAxis(), 0);
		assertEquals(298.257222101, ellipsoid.getInverseFlattening(), 0);
		reader.close();
		assertEquals(text.replaceAll("SPHEROID", "ELLIPSOID"),
				ellipsoid.toString());

		text = "ELLIPSOID[\"Clark 1866\",20925832.164,294.97869821,"
				+ "LENGTHUNIT[\"US survey foot\",0.304800609601219]]";
		reader = new CRSReader(text);
		ellipsoid = reader.readEllipsoid();
		assertEquals("Clark 1866", ellipsoid.getName());
		assertEquals(20925832.164, ellipsoid.getSemiMajorAxis(), 0);
		assertEquals(294.97869821, ellipsoid.getInverseFlattening(), 0);
		assertEquals(UnitType.LENGTHUNIT, ellipsoid.getUnit().getType());
		assertEquals("US survey foot", ellipsoid.getUnit().getName());
		assertEquals(0.304800609601219,
				ellipsoid.getUnit().getConversionFactor(), 0);
		reader.close();
		assertEquals(text.replaceAll("20925832.164", "2.0925832164E7"),
				ellipsoid.toString());

		text = "ELLIPSOID[\"Sphere\",6371000,0,LENGTHUNIT[\"metre\",1.0]]";
		reader = new CRSReader(text);
		ellipsoid = reader.readEllipsoid();
		assertEquals("Sphere", ellipsoid.getName());
		assertEquals(6371000, ellipsoid.getSemiMajorAxis(), 0);
		assertEquals(0, ellipsoid.getInverseFlattening(), 0);
		assertEquals(UnitType.LENGTHUNIT, ellipsoid.getUnit().getType());
		assertEquals("metre", ellipsoid.getUnit().getName());
		assertEquals(1.0, ellipsoid.getUnit().getConversionFactor(), 0);
		reader.close();
		assertEquals(text.replaceAll("6371000,0", "6371000.0,0.0"),
				ellipsoid.toString());

	}

	/**
	 * Test prime meridian
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testPrimeMeridian() throws IOException {

		String text = "PRIMEM[\"Paris\",2.5969213,ANGLEUNIT[\"grad\",0.015707963267949]]";
		CRSReader reader = new CRSReader(text);
		PrimeMeridian primeMeridian = reader.readPrimeMeridian();
		assertEquals("Paris", primeMeridian.getName());
		assertEquals(2.5969213, primeMeridian.getIrmLongitude(), 0);
		assertEquals(UnitType.ANGLEUNIT,
				primeMeridian.getIrmLongitudeUnit().getType());
		assertEquals("grad", primeMeridian.getIrmLongitudeUnit().getName());
		assertEquals(0.015707963267949,
				primeMeridian.getIrmLongitudeUnit().getConversionFactor(), 0);
		reader.close();
		assertEquals(text, primeMeridian.toString());

		text = "PRIMEM[\"Ferro\",-17.6666667]";
		reader = new CRSReader(text);
		primeMeridian = reader.readPrimeMeridian();
		assertEquals("Ferro", primeMeridian.getName());
		assertEquals(-17.6666667, primeMeridian.getIrmLongitude(), 0);
		reader.close();
		assertEquals(text, primeMeridian.toString());

		text = "PRIMEM[\"Greenwich\",0.0,ANGLEUNIT[\"degree\",0.0174532925199433]]";
		reader = new CRSReader(text);
		primeMeridian = reader.readPrimeMeridian();
		assertEquals("Greenwich", primeMeridian.getName());
		assertEquals(0.0, primeMeridian.getIrmLongitude(), 0);
		assertEquals(UnitType.ANGLEUNIT,
				primeMeridian.getIrmLongitudeUnit().getType());
		assertEquals("degree", primeMeridian.getIrmLongitudeUnit().getName());
		assertEquals(0.0174532925199433,
				primeMeridian.getIrmLongitudeUnit().getConversionFactor(), 0);
		reader.close();
		assertEquals(text, primeMeridian.toString());

	}

	/**
	 * Test geodetic reference frame
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testGeodeticReferenceFrame() throws IOException {

		String text = "DATUM[\"North American Datum 1983\","
				+ "ELLIPSOID[\"GRS 1980\",6378137,298.257222101,LENGTHUNIT[\"metre\",1.0]]]";
		CRSReader reader = new CRSReader(text);
		GeodeticReferenceFrame geodeticReferenceFrame = reader
				.readGeodeticReferenceFrame();
		assertEquals("North American Datum 1983",
				geodeticReferenceFrame.getName());
		Ellipsoid ellipsoid = geodeticReferenceFrame.getEllipsoid();
		assertEquals("GRS 1980", ellipsoid.getName());
		assertEquals(6378137, ellipsoid.getSemiMajorAxis(), 0);
		assertEquals(298.257222101, ellipsoid.getInverseFlattening(), 0);
		assertEquals(UnitType.LENGTHUNIT, ellipsoid.getUnit().getType());
		assertEquals("metre", ellipsoid.getUnit().getName());
		assertEquals(1.0, ellipsoid.getUnit().getConversionFactor(), 0);
		reader.close();
		assertEquals(text.replaceAll("6378137", "6378137.0"),
				geodeticReferenceFrame.toString());

		text = "TRF[\"World Geodetic System 1984\","
				+ "ELLIPSOID[\"WGS 84\",6378388.0,298.257223563,LENGTHUNIT[\"metre\",1.0]]"
				+ "],PRIMEM[\"Greenwich\",0.0]";
		reader = new CRSReader(text);
		geodeticReferenceFrame = reader.readGeodeticReferenceFrame();
		assertEquals("World Geodetic System 1984",
				geodeticReferenceFrame.getName());
		ellipsoid = geodeticReferenceFrame.getEllipsoid();
		assertEquals("WGS 84", ellipsoid.getName());
		assertEquals(6378388.0, ellipsoid.getSemiMajorAxis(), 0);
		assertEquals(298.257223563, ellipsoid.getInverseFlattening(), 0);
		assertEquals(UnitType.LENGTHUNIT, ellipsoid.getUnit().getType());
		assertEquals("metre", ellipsoid.getUnit().getName());
		assertEquals(1.0, ellipsoid.getUnit().getConversionFactor(), 0);
		assertEquals("Greenwich",
				geodeticReferenceFrame.getPrimeMeridian().getName());
		assertEquals(0.0,
				geodeticReferenceFrame.getPrimeMeridian().getIrmLongitude(), 0);
		reader.close();
		assertEquals(text.replaceAll("TRF", "DATUM"),
				geodeticReferenceFrame.toString());

		text = "GEODETICDATUM[\"Tananarive 1925\","
				+ "ELLIPSOID[\"International 1924\",6378388.0,297.0,LENGTHUNIT[\"metre\",1.0]],"
				+ "ANCHOR[\"Tananarive observatory:21.0191667gS, 50.23849537gE of Paris\"]],"
				+ "PRIMEM[\"Paris\",2.5969213,ANGLEUNIT[\"grad\",0.015707963267949]]";
		reader = new CRSReader(text);
		geodeticReferenceFrame = reader.readGeodeticReferenceFrame();
		assertEquals("Tananarive 1925", geodeticReferenceFrame.getName());
		ellipsoid = geodeticReferenceFrame.getEllipsoid();
		assertEquals("International 1924", ellipsoid.getName());
		assertEquals(6378388.0, ellipsoid.getSemiMajorAxis(), 0);
		assertEquals(297.0, ellipsoid.getInverseFlattening(), 0);
		assertEquals(UnitType.LENGTHUNIT, ellipsoid.getUnit().getType());
		assertEquals("metre", ellipsoid.getUnit().getName());
		assertEquals(1.0, ellipsoid.getUnit().getConversionFactor(), 0);
		assertEquals(
				"Tananarive observatory:21.0191667gS, 50.23849537gE of Paris",
				geodeticReferenceFrame.getAnchor());
		assertEquals("Paris",
				geodeticReferenceFrame.getPrimeMeridian().getName());
		assertEquals(2.5969213,
				geodeticReferenceFrame.getPrimeMeridian().getIrmLongitude(), 0);
		assertEquals(UnitType.ANGLEUNIT, geodeticReferenceFrame
				.getPrimeMeridian().getIrmLongitudeUnit().getType());
		assertEquals("grad", geodeticReferenceFrame.getPrimeMeridian()
				.getIrmLongitudeUnit().getName());
		assertEquals(0.015707963267949, geodeticReferenceFrame
				.getPrimeMeridian().getIrmLongitudeUnit().getConversionFactor(),
				0);
		reader.close();
		assertEquals(text.replaceAll("GEODETICDATUM", "DATUM"),
				geodeticReferenceFrame.toString());

	}

	/**
	 * Test geodetic coordinate reference system
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testGeodeticCoordinateReferenceSystem() throws IOException {

		String text = "GEODCRS[\"JGD2000\","
				+ "DATUM[\"Japanese Geodetic Datum 2000\","
				+ "ELLIPSOID[\"GRS 1980\",6378137,298.257222101]],"
				+ "CS[Cartesian,3],AXIS[\"(X)\",geocentricX],"
				+ "AXIS[\"(Y)\",geocentricY],AXIS[\"(Z)\",geocentricZ],"
				+ "LENGTHUNIT[\"metre\",1.0],"
				+ "USAGE[SCOPE[\"Geodesy, topographic mapping and cadastre\"],"
				+ "AREA[\"Japan\"],BBOX[17.09,122.38,46.05,157.64],"
				+ "TIMEEXTENT[2002-04-01,2011-10-21]],"
				+ "ID[\"EPSG\",4946,URI[\"urn:ogc:def:crs:EPSG::4946\"]],"
				+ "REMARK[\"注：JGD2000ジオセントリックは現在JGD2011に代わりました。\"]]";

		CoordinateReferenceSystem crs = CRSReader.read(text, true);
		GeodeticCoordinateReferenceSystem geodeticOrGeographicCrs = CRSReader
				.readGeodeticOrGeographic(text);
		assertEquals(crs, geodeticOrGeographicCrs);
		GeodeticCoordinateReferenceSystem geodeticCrs = CRSReader
				.readGeodetic(text);
		assertEquals(crs, geodeticCrs);
		assertEquals(CoordinateReferenceSystemType.GEODETIC,
				geodeticCrs.getType());
		assertEquals("JGD2000", geodeticCrs.getName());
		assertEquals("Japanese Geodetic Datum 2000",
				geodeticCrs.getGeodeticReferenceFrame().getName());
		assertEquals("GRS 1980", geodeticCrs.getGeodeticReferenceFrame()
				.getEllipsoid().getName());
		assertEquals(6378137, geodeticCrs.getGeodeticReferenceFrame()
				.getEllipsoid().getSemiMajorAxis(), 0);
		assertEquals(298.257222101, geodeticCrs.getGeodeticReferenceFrame()
				.getEllipsoid().getInverseFlattening(), 0);
		assertEquals(CoordinateSystemType.CARTESIAN,
				geodeticCrs.getCoordinateSystem().getType());
		assertEquals(3, geodeticCrs.getCoordinateSystem().getDimension());
		assertEquals("X", geodeticCrs.getCoordinateSystem().getAxes().get(0)
				.getAbbreviation());
		assertEquals(AxisDirectionType.GEOCENTRIC_X, geodeticCrs
				.getCoordinateSystem().getAxes().get(0).getDirection());
		assertEquals("Y", geodeticCrs.getCoordinateSystem().getAxes().get(1)
				.getAbbreviation());
		assertEquals(AxisDirectionType.GEOCENTRIC_Y, geodeticCrs
				.getCoordinateSystem().getAxes().get(1).getDirection());
		assertEquals("Z", geodeticCrs.getCoordinateSystem().getAxes().get(2)
				.getAbbreviation());
		assertEquals(AxisDirectionType.GEOCENTRIC_Z, geodeticCrs
				.getCoordinateSystem().getAxes().get(2).getDirection());
		assertEquals(UnitType.LENGTHUNIT,
				geodeticCrs.getCoordinateSystem().getUnit().getType());
		assertEquals("metre",
				geodeticCrs.getCoordinateSystem().getUnit().getName());
		assertEquals(1.0, geodeticCrs.getCoordinateSystem().getUnit()
				.getConversionFactor(), 0);
		assertEquals("Geodesy, topographic mapping and cadastre",
				geodeticCrs.getUsages().get(0).getScope());
		assertEquals("Japan", geodeticCrs.getUsages().get(0).getExtent()
				.getAreaDescription());
		assertEquals(17.09, geodeticCrs.getUsages().get(0).getExtent()
				.getGeographicBoundingBox().getLowerLeftLatitude(), 0);
		assertEquals(122.38, geodeticCrs.getUsages().get(0).getExtent()
				.getGeographicBoundingBox().getLowerLeftLongitude(), 0);
		assertEquals(46.05, geodeticCrs.getUsages().get(0).getExtent()
				.getGeographicBoundingBox().getUpperRightLatitude(), 0);
		assertEquals(157.64,
				geodeticCrs.getUsages().get(0).getExtent()
						.getGeographicBoundingBox().getUpperRightLongitude(),
				0);
		assertEquals("2002-04-01", geodeticCrs.getUsages().get(0).getExtent()
				.getTemporalExtent().getStart());
		assertTrue(geodeticCrs.getUsages().get(0).getExtent()
				.getTemporalExtent().hasStartDateTime());
		assertEquals("2002-04-01", geodeticCrs.getUsages().get(0).getExtent()
				.getTemporalExtent().getStartDateTime().toString());
		assertEquals("2011-10-21", geodeticCrs.getUsages().get(0).getExtent()
				.getTemporalExtent().getEnd());
		assertTrue(geodeticCrs.getUsages().get(0).getExtent()
				.getTemporalExtent().hasEndDateTime());
		assertEquals("2011-10-21", geodeticCrs.getUsages().get(0).getExtent()
				.getTemporalExtent().getEndDateTime().toString());
		assertEquals("EPSG", geodeticCrs.getIdentifiers().get(0).getName());
		assertEquals("4946",
				geodeticCrs.getIdentifiers().get(0).getUniqueIdentifier());
		assertEquals("urn:ogc:def:crs:EPSG::4946",
				geodeticCrs.getIdentifiers().get(0).getUri());
		assertEquals("注：JGD2000ジオセントリックは現在JGD2011に代わりました。",
				geodeticCrs.getRemark());
		text = text.replaceAll("6378137", "6378137.0");
		assertEquals(text, geodeticCrs.toString());
		assertEquals(text, CRSWriter.write(geodeticCrs));
		assertEquals(WKTUtils.pretty(text), CRSWriter.writePretty(geodeticCrs));

	}

	/**
	 * Test geographic coordinate reference system
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testGeographicCoordinateReferenceSystem() throws IOException {

		String text = "GEOGCRS[\"WGS 84 (G1762)\","
				+ "DYNAMIC[FRAMEEPOCH[2005.0]],"
				+ "TRF[\"World Geodetic System 1984 (G1762)\","
				+ "ELLIPSOID[\"WGS 84\",6378137,298.257223563,LENGTHUNIT[\"metre\",1.0]]],"
				+ "CS[ellipsoidal,3],"
				+ "AXIS[\"(lat)\",north,ANGLEUNIT[\"degree\",0.0174532925199433]],"
				+ "AXIS[\"(lon)\",east,ANGLEUNIT[\"degree\",0.0174532925199433]],"
				+ "AXIS[\"ellipsoidal height (h)\",up,LENGTHUNIT[\"metre\",1.0]]]";

		CoordinateReferenceSystem crs = CRSReader.read(text, true);
		GeodeticCoordinateReferenceSystem geodeticOrGeographicCrs = CRSReader
				.readGeodeticOrGeographic(text);
		assertEquals(crs, geodeticOrGeographicCrs);
		GeodeticCoordinateReferenceSystem geographicCrs = CRSReader
				.readGeographic(text);
		assertEquals(crs, geographicCrs);
		assertEquals(CoordinateReferenceSystemType.GEOGRAPHIC,
				geographicCrs.getType());
		assertEquals("WGS 84 (G1762)", geographicCrs.getName());
		assertEquals(2005.0, geographicCrs.getDynamic().getReferenceEpoch(), 0);
		assertEquals("World Geodetic System 1984 (G1762)",
				geographicCrs.getGeodeticReferenceFrame().getName());
		assertEquals("WGS 84", geographicCrs.getGeodeticReferenceFrame()
				.getEllipsoid().getName());
		assertEquals(6378137, geographicCrs.getGeodeticReferenceFrame()
				.getEllipsoid().getSemiMajorAxis(), 0);
		assertEquals(298.257223563, geographicCrs.getGeodeticReferenceFrame()
				.getEllipsoid().getInverseFlattening(), 0);
		assertEquals(UnitType.LENGTHUNIT,
				geographicCrs.getGeodeticReferenceFrame().getEllipsoid()
						.getUnit().getType());
		assertEquals("metre", geographicCrs.getGeodeticReferenceFrame()
				.getEllipsoid().getUnit().getName());
		assertEquals(1.0, geographicCrs.getGeodeticReferenceFrame()
				.getEllipsoid().getUnit().getConversionFactor(), 0);
		assertEquals(CoordinateSystemType.ELLIPSOIDAL,
				geographicCrs.getCoordinateSystem().getType());
		assertEquals(3, geographicCrs.getCoordinateSystem().getDimension());
		assertEquals("lat", geographicCrs.getCoordinateSystem().getAxes().get(0)
				.getAbbreviation());
		assertEquals(AxisDirectionType.NORTH, geographicCrs
				.getCoordinateSystem().getAxes().get(0).getDirection());
		assertEquals(UnitType.ANGLEUNIT, geographicCrs.getCoordinateSystem()
				.getAxes().get(0).getUnit().getType());
		assertEquals("degree", geographicCrs.getCoordinateSystem().getAxes()
				.get(0).getUnit().getName());
		assertEquals(0.0174532925199433, geographicCrs.getCoordinateSystem()
				.getAxes().get(0).getUnit().getConversionFactor(), 0);
		assertEquals("lon", geographicCrs.getCoordinateSystem().getAxes().get(1)
				.getAbbreviation());
		assertEquals(AxisDirectionType.EAST, geographicCrs.getCoordinateSystem()
				.getAxes().get(1).getDirection());
		assertEquals(UnitType.ANGLEUNIT, geographicCrs.getCoordinateSystem()
				.getAxes().get(1).getUnit().getType());
		assertEquals("degree", geographicCrs.getCoordinateSystem().getAxes()
				.get(1).getUnit().getName());
		assertEquals(0.0174532925199433, geographicCrs.getCoordinateSystem()
				.getAxes().get(1).getUnit().getConversionFactor(), 0);
		assertEquals("ellipsoidal height",
				geographicCrs.getCoordinateSystem().getAxes().get(2).getName());
		assertEquals("h", geographicCrs.getCoordinateSystem().getAxes().get(2)
				.getAbbreviation());
		assertEquals(AxisDirectionType.UP, geographicCrs.getCoordinateSystem()
				.getAxes().get(2).getDirection());
		assertEquals(UnitType.LENGTHUNIT, geographicCrs.getCoordinateSystem()
				.getAxes().get(2).getUnit().getType());
		assertEquals("metre", geographicCrs.getCoordinateSystem().getAxes()
				.get(2).getUnit().getName());
		assertEquals(1.0, geographicCrs.getCoordinateSystem().getAxes().get(2)
				.getUnit().getConversionFactor(), 0);
		text = text.replaceAll("TRF", "DATUM").replaceAll("6378137",
				"6378137.0");
		assertEquals(text, geographicCrs.toString());
		assertEquals(text, CRSWriter.write(geographicCrs));
		assertEquals(WKTUtils.pretty(text),
				CRSWriter.writePretty(geographicCrs));

		text = "GEOGRAPHICCRS[\"NAD83\","
				+ "DATUM[\"North American Datum 1983\","
				+ "ELLIPSOID[\"GRS 1980\",6378137,298.257222101,LENGTHUNIT[\"metre\",1.0]]],"
				+ "CS[ellipsoidal,2],AXIS[\"latitude\",north],"
				+ "AXIS[\"longitude\",east],"
				+ "ANGLEUNIT[\"degree\",0.017453292519943],"
				+ "ID[\"EPSG\",4269],REMARK[\"1986 realisation\"]]";

		crs = CRSReader.read(text, true);
		geodeticOrGeographicCrs = CRSReader.readGeodeticOrGeographic(text);
		assertEquals(crs, geodeticOrGeographicCrs);
		geographicCrs = CRSReader.readGeographic(text);
		assertEquals(crs, geographicCrs);
		assertEquals(CoordinateReferenceSystemType.GEOGRAPHIC,
				geographicCrs.getType());
		assertEquals("NAD83", geographicCrs.getName());
		assertEquals("North American Datum 1983",
				geographicCrs.getGeodeticReferenceFrame().getName());
		assertEquals("GRS 1980", geographicCrs.getGeodeticReferenceFrame()
				.getEllipsoid().getName());
		assertEquals(6378137, geographicCrs.getGeodeticReferenceFrame()
				.getEllipsoid().getSemiMajorAxis(), 0);
		assertEquals(298.257222101, geographicCrs.getGeodeticReferenceFrame()
				.getEllipsoid().getInverseFlattening(), 0);
		assertEquals(UnitType.LENGTHUNIT,
				geographicCrs.getGeodeticReferenceFrame().getEllipsoid()
						.getUnit().getType());
		assertEquals("metre", geographicCrs.getGeodeticReferenceFrame()
				.getEllipsoid().getUnit().getName());
		assertEquals(1.0, geographicCrs.getGeodeticReferenceFrame()
				.getEllipsoid().getUnit().getConversionFactor(), 0);
		assertEquals(CoordinateSystemType.ELLIPSOIDAL,
				geographicCrs.getCoordinateSystem().getType());
		assertEquals(2, geographicCrs.getCoordinateSystem().getDimension());
		assertEquals("latitude",
				geographicCrs.getCoordinateSystem().getAxes().get(0).getName());
		assertEquals(AxisDirectionType.NORTH, geographicCrs
				.getCoordinateSystem().getAxes().get(0).getDirection());
		assertEquals("longitude",
				geographicCrs.getCoordinateSystem().getAxes().get(1).getName());
		assertEquals(AxisDirectionType.EAST, geographicCrs.getCoordinateSystem()
				.getAxes().get(1).getDirection());
		assertEquals(UnitType.ANGLEUNIT,
				geographicCrs.getCoordinateSystem().getUnit().getType());
		assertEquals("degree",
				geographicCrs.getCoordinateSystem().getUnit().getName());
		assertEquals(0.017453292519943, geographicCrs.getCoordinateSystem()
				.getUnit().getConversionFactor(), 0);
		assertEquals("EPSG", geographicCrs.getIdentifiers().get(0).getName());
		assertEquals("4269",
				geographicCrs.getIdentifiers().get(0).getUniqueIdentifier());
		assertEquals("1986 realisation", geographicCrs.getRemark());
		text = text.replaceAll("GEOGRAPHICCRS", "GEOGCRS").replaceAll("6378137",
				"6378137.0");
		assertEquals(text, geographicCrs.toString());
		assertEquals(text, CRSWriter.write(geographicCrs));
		assertEquals(WKTUtils.pretty(text),
				CRSWriter.writePretty(geographicCrs));

		text = "GEOGCRS[\"NTF (Paris)\","
				+ "DATUM[\"Nouvelle Triangulation Francaise\","
				+ "ELLIPSOID[\"Clarke 1880 (IGN)\",6378249.2,293.4660213]],"
				+ "PRIMEM[\"Paris\",2.5969213],CS[ellipsoidal,2],"
				+ "AXIS[\"latitude\",north,ORDER[1]],"
				+ "AXIS[\"longitude\",east,ORDER[2]],"
				+ "ANGLEUNIT[\"grad\",0.015707963267949],"
				+ "REMARK[\"Nouvelle Triangulation Française\"]]";

		crs = CRSReader.read(text, true);
		geodeticOrGeographicCrs = CRSReader.readGeodeticOrGeographic(text);
		assertEquals(crs, geodeticOrGeographicCrs);
		geographicCrs = CRSReader.readGeographic(text);
		assertEquals(crs, geographicCrs);
		assertEquals(CoordinateReferenceSystemType.GEOGRAPHIC,
				geographicCrs.getType());
		assertEquals("NTF (Paris)", geographicCrs.getName());
		assertEquals("Nouvelle Triangulation Francaise",
				geographicCrs.getGeodeticReferenceFrame().getName());
		assertEquals("Clarke 1880 (IGN)", geographicCrs
				.getGeodeticReferenceFrame().getEllipsoid().getName());
		assertEquals(6378249.2, geographicCrs.getGeodeticReferenceFrame()
				.getEllipsoid().getSemiMajorAxis(), 0);
		assertEquals(293.4660213, geographicCrs.getGeodeticReferenceFrame()
				.getEllipsoid().getInverseFlattening(), 0);
		assertEquals("Paris", geographicCrs.getGeodeticReferenceFrame()
				.getPrimeMeridian().getName());
		assertEquals(2.5969213, geographicCrs.getGeodeticReferenceFrame()
				.getPrimeMeridian().getIrmLongitude(), 0);
		assertEquals(CoordinateSystemType.ELLIPSOIDAL,
				geographicCrs.getCoordinateSystem().getType());
		assertEquals(2, geographicCrs.getCoordinateSystem().getDimension());
		assertEquals("latitude",
				geographicCrs.getCoordinateSystem().getAxes().get(0).getName());
		assertEquals(AxisDirectionType.NORTH, geographicCrs
				.getCoordinateSystem().getAxes().get(0).getDirection());
		assertEquals(1, geographicCrs.getCoordinateSystem().getAxes().get(0)
				.getOrder().intValue());
		assertEquals("longitude",
				geographicCrs.getCoordinateSystem().getAxes().get(1).getName());
		assertEquals(AxisDirectionType.EAST, geographicCrs.getCoordinateSystem()
				.getAxes().get(1).getDirection());
		assertEquals(2, geographicCrs.getCoordinateSystem().getAxes().get(1)
				.getOrder().intValue());
		assertEquals(UnitType.ANGLEUNIT,
				geographicCrs.getCoordinateSystem().getUnit().getType());
		assertEquals("grad",
				geographicCrs.getCoordinateSystem().getUnit().getName());
		assertEquals(0.015707963267949, geographicCrs.getCoordinateSystem()
				.getUnit().getConversionFactor(), 0);
		assertEquals("Nouvelle Triangulation Française",
				geographicCrs.getRemark());
		assertEquals(text, geographicCrs.toString());
		assertEquals(text, CRSWriter.write(geographicCrs));
		assertEquals(WKTUtils.pretty(text),
				CRSWriter.writePretty(geographicCrs));

	}

	/**
	 * Test map projection
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testMapProjection() throws IOException {

		String text = "CONVERSION[\"UTM zone 10N\","
				+ "METHOD[\"Transverse Mercator\",ID[\"EPSG\",9807]],"
				+ "PARAMETER[\"Latitude of natural origin\",0,"
				+ "ANGLEUNIT[\"degree\",0.0174532925199433],"
				+ "ID[\"EPSG\",8801]],"
				+ "PARAMETER[\"Longitude of natural origin\",-123,"
				+ "ANGLEUNIT[\"degree\",0.0174532925199433],ID[\"EPSG\",8802]],"
				+ "PARAMETER[\"Scale factor at natural origin\",0.9996,"
				+ "SCALEUNIT[\"unity\",1.0],ID[\"EPSG\",8805]],"
				+ "PARAMETER[\"False easting\",500000,"
				+ "LENGTHUNIT[\"metre\",1.0],ID[\"EPSG\",8806]],"
				+ "PARAMETER[\"False northing\",0,LENGTHUNIT[\"metre\",1.0],ID[\"EPSG\",8807]]]";
		CRSReader reader = new CRSReader(text);
		MapProjection mapProjection = reader.readMapProjection();
		assertEquals("UTM zone 10N", mapProjection.getName());
		assertEquals("Transverse Mercator", mapProjection.getMethodName());
		assertEquals("EPSG",
				mapProjection.getMethodIdentifiers().get(0).getName());
		assertEquals("9807", mapProjection.getMethodIdentifiers().get(0)
				.getUniqueIdentifier());
		assertEquals("Latitude of natural origin",
				mapProjection.getParameters().get(0).getName());
		assertEquals(0, mapProjection.getParameters().get(0).getValue(), 0);
		assertEquals(UnitType.ANGLEUNIT,
				mapProjection.getParameters().get(0).getUnit().getType());
		assertEquals("degree",
				mapProjection.getParameters().get(0).getUnit().getName());
		assertEquals(0.0174532925199433, mapProjection.getParameters().get(0)
				.getUnit().getConversionFactor(), 0);
		assertEquals("EPSG", mapProjection.getParameters().get(0)
				.getIdentifiers().get(0).getName());
		assertEquals("8801", mapProjection.getParameters().get(0)
				.getIdentifiers().get(0).getUniqueIdentifier());
		assertEquals("Longitude of natural origin",
				mapProjection.getParameters().get(1).getName());
		assertEquals(-123, mapProjection.getParameters().get(1).getValue(), 0);
		assertEquals(UnitType.ANGLEUNIT,
				mapProjection.getParameters().get(1).getUnit().getType());
		assertEquals("degree",
				mapProjection.getParameters().get(1).getUnit().getName());
		assertEquals(0.0174532925199433, mapProjection.getParameters().get(1)
				.getUnit().getConversionFactor(), 0);
		assertEquals("EPSG", mapProjection.getParameters().get(1)
				.getIdentifiers().get(0).getName());
		assertEquals("8802", mapProjection.getParameters().get(1)
				.getIdentifiers().get(0).getUniqueIdentifier());
		assertEquals("Scale factor at natural origin",
				mapProjection.getParameters().get(2).getName());
		assertEquals(0.9996, mapProjection.getParameters().get(2).getValue(),
				0);
		assertEquals(UnitType.SCALEUNIT,
				mapProjection.getParameters().get(2).getUnit().getType());
		assertEquals("unity",
				mapProjection.getParameters().get(2).getUnit().getName());
		assertEquals(1.0, mapProjection.getParameters().get(2).getUnit()
				.getConversionFactor(), 0);
		assertEquals("EPSG", mapProjection.getParameters().get(2)
				.getIdentifiers().get(0).getName());
		assertEquals("8805", mapProjection.getParameters().get(2)
				.getIdentifiers().get(0).getUniqueIdentifier());
		assertEquals("False easting",
				mapProjection.getParameters().get(3).getName());
		assertEquals(500000, mapProjection.getParameters().get(3).getValue(),
				0);
		assertEquals(UnitType.LENGTHUNIT,
				mapProjection.getParameters().get(3).getUnit().getType());
		assertEquals("metre",
				mapProjection.getParameters().get(3).getUnit().getName());
		assertEquals(1.0, mapProjection.getParameters().get(3).getUnit()
				.getConversionFactor(), 0);
		assertEquals("EPSG", mapProjection.getParameters().get(3)
				.getIdentifiers().get(0).getName());
		assertEquals("8806", mapProjection.getParameters().get(3)
				.getIdentifiers().get(0).getUniqueIdentifier());
		assertEquals("False northing",
				mapProjection.getParameters().get(4).getName());
		assertEquals(0, mapProjection.getParameters().get(4).getValue(), 0);
		assertEquals(UnitType.LENGTHUNIT,
				mapProjection.getParameters().get(4).getUnit().getType());
		assertEquals("metre",
				mapProjection.getParameters().get(4).getUnit().getName());
		assertEquals(1.0, mapProjection.getParameters().get(4).getUnit()
				.getConversionFactor(), 0);
		assertEquals("EPSG", mapProjection.getParameters().get(4)
				.getIdentifiers().get(0).getName());
		assertEquals("8807", mapProjection.getParameters().get(4)
				.getIdentifiers().get(0).getUniqueIdentifier());
		reader.close();
		assertEquals(
				text.replaceAll(",0,", ",0.0,").replaceAll("-123", "-123.0")
						.replaceAll("500000", "500000.0"),
				mapProjection.toString());

		text = "CONVERSION[\"UTM zone 10N\","
				+ "METHOD[\"Transverse Mercator\"],"
				+ "PARAMETER[\"Latitude of natural origin\",0,"
				+ "ANGLEUNIT[\"degree\",0.0174532925199433]],"
				+ "PARAMETER[\"Longitude of natural origin\",-123,"
				+ "ANGLEUNIT[\"degree\",0.0174532925199433]],"
				+ "PARAMETER[\"Scale factor at natural origin\",0.9996,"
				+ "SCALEUNIT[\"unity\",1.0]],"
				+ "PARAMETER[\"False easting\",500000,LENGTHUNIT[\"metre\",1.0]],"
				+ "PARAMETER[\"False northing\",0,LENGTHUNIT[\"metre\",1.0]],"
				+ "ID[\"EPSG\",16010]]";
		reader = new CRSReader(text);
		mapProjection = reader.readMapProjection();
		assertEquals("UTM zone 10N", mapProjection.getName());
		assertEquals("Transverse Mercator", mapProjection.getMethodName());
		assertEquals("Latitude of natural origin",
				mapProjection.getParameters().get(0).getName());
		assertEquals(0, mapProjection.getParameters().get(0).getValue(), 0);
		assertEquals(UnitType.ANGLEUNIT,
				mapProjection.getParameters().get(0).getUnit().getType());
		assertEquals("degree",
				mapProjection.getParameters().get(0).getUnit().getName());
		assertEquals(0.0174532925199433, mapProjection.getParameters().get(0)
				.getUnit().getConversionFactor(), 0);
		assertEquals("Longitude of natural origin",
				mapProjection.getParameters().get(1).getName());
		assertEquals(-123, mapProjection.getParameters().get(1).getValue(), 0);
		assertEquals(UnitType.ANGLEUNIT,
				mapProjection.getParameters().get(1).getUnit().getType());
		assertEquals("degree",
				mapProjection.getParameters().get(1).getUnit().getName());
		assertEquals(0.0174532925199433, mapProjection.getParameters().get(1)
				.getUnit().getConversionFactor(), 0);
		assertEquals("Scale factor at natural origin",
				mapProjection.getParameters().get(2).getName());
		assertEquals(0.9996, mapProjection.getParameters().get(2).getValue(),
				0);
		assertEquals(UnitType.SCALEUNIT,
				mapProjection.getParameters().get(2).getUnit().getType());
		assertEquals("unity",
				mapProjection.getParameters().get(2).getUnit().getName());
		assertEquals(1.0, mapProjection.getParameters().get(2).getUnit()
				.getConversionFactor(), 0);
		assertEquals("False easting",
				mapProjection.getParameters().get(3).getName());
		assertEquals(500000, mapProjection.getParameters().get(3).getValue(),
				0);
		assertEquals(UnitType.LENGTHUNIT,
				mapProjection.getParameters().get(3).getUnit().getType());
		assertEquals("metre",
				mapProjection.getParameters().get(3).getUnit().getName());
		assertEquals(1.0, mapProjection.getParameters().get(3).getUnit()
				.getConversionFactor(), 0);
		assertEquals("False northing",
				mapProjection.getParameters().get(4).getName());
		assertEquals(0, mapProjection.getParameters().get(4).getValue(), 0);
		assertEquals(UnitType.LENGTHUNIT,
				mapProjection.getParameters().get(4).getUnit().getType());
		assertEquals("metre",
				mapProjection.getParameters().get(4).getUnit().getName());
		assertEquals(1.0, mapProjection.getParameters().get(4).getUnit()
				.getConversionFactor(), 0);
		assertEquals("EPSG", mapProjection.getIdentifiers().get(0).getName());
		assertEquals("16010",
				mapProjection.getIdentifiers().get(0).getUniqueIdentifier());
		reader.close();
		assertEquals(
				text.replaceAll(",0,", ",0.0,").replaceAll("-123", "-123.0")
						.replaceAll("500000", "500000.0"),
				mapProjection.toString());

	}

	/**
	 * Test projected geographic coordinate reference system
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testProjectedGeographicCoordinateReferenceSystem()
			throws IOException {

		String text = "PROJCRS[\"ETRS89 Lambert Azimuthal Equal Area CRS\",BASEGEOGCRS[\"ETRS89\","
				+ "DATUM[\"ETRS89\","
				+ "ELLIPSOID[\"GRS 80\",6378137,298.257222101,LENGTHUNIT[\"metre\",1.0]]"
				+ "],ID[\"EuroGeographics\",\"ETRS89-LatLon\"]],"
				+ "CONVERSION[\"LAEA\","
				+ "METHOD[\"Lambert Azimuthal Equal Area\",ID[\"EPSG\",9820]],"
				+ "PARAMETER[\"Latitude of origin\",52.0,"
				+ "ANGLEUNIT[\"degree\",0.0174532925199433]],"
				+ "PARAMETER[\"Longitude of origin\",10.0,"
				+ "ANGLEUNIT[\"degree\",0.0174532925199433]],"
				+ "PARAMETER[\"False easting\",4321000.0,LENGTHUNIT[\"metre\",1.0]],"
				+ "PARAMETER[\"False northing\",3210000.0,LENGTHUNIT[\"metre\",1.0]]"
				+ "],CS[Cartesian,2],AXIS[\"(Y)\",north,ORDER[1]],"
				+ "AXIS[\"(X)\",east,ORDER[2]],LENGTHUNIT[\"metre\",1.0],"
				+ "USAGE[SCOPE[\"Description of a purpose\"],AREA[\"An area description\"]],"
				+ "ID[\"EuroGeographics\",\"ETRS-LAEA\"]]";

		CoordinateReferenceSystem crs = CRSReader.read(text, true);
		ProjectedCoordinateReferenceSystem projectedCrs = CRSReader
				.readProjected(text);
		assertEquals(crs, projectedCrs);
		ProjectedCoordinateReferenceSystem projectedGeographicCrs = CRSReader
				.readProjectedGeographic(text);
		assertEquals(crs, projectedGeographicCrs);
		assertEquals(CoordinateReferenceSystemType.PROJECTED,
				projectedGeographicCrs.getType());
		assertEquals("ETRS89 Lambert Azimuthal Equal Area CRS",
				projectedGeographicCrs.getName());
		assertEquals(CoordinateReferenceSystemType.GEOGRAPHIC,
				projectedGeographicCrs.getBaseType());
		assertEquals("ETRS89", projectedGeographicCrs.getBaseName());
		assertEquals("ETRS89",
				projectedGeographicCrs.getGeodeticReferenceFrame().getName());
		assertEquals("GRS 80", projectedGeographicCrs
				.getGeodeticReferenceFrame().getEllipsoid().getName());
		assertEquals(6378137, projectedGeographicCrs.getGeodeticReferenceFrame()
				.getEllipsoid().getSemiMajorAxis(), 0);
		assertEquals(298.257222101,
				projectedGeographicCrs.getGeodeticReferenceFrame()
						.getEllipsoid().getInverseFlattening(),
				0);
		assertEquals(UnitType.LENGTHUNIT,
				projectedGeographicCrs.getGeodeticReferenceFrame()
						.getEllipsoid().getUnit().getType());
		assertEquals("metre", projectedGeographicCrs.getGeodeticReferenceFrame()
				.getEllipsoid().getUnit().getName());
		assertEquals(1.0, projectedGeographicCrs.getGeodeticReferenceFrame()
				.getEllipsoid().getUnit().getConversionFactor(), 0);
		assertEquals("EuroGeographics",
				projectedGeographicCrs.getBaseIdentifiers().get(0).getName());
		assertEquals("ETRS89-LatLon", projectedGeographicCrs
				.getBaseIdentifiers().get(0).getUniqueIdentifier());
		assertEquals("LAEA",
				projectedGeographicCrs.getMapProjection().getName());
		assertEquals("Lambert Azimuthal Equal Area",
				projectedGeographicCrs.getMapProjection().getMethodName());
		assertEquals("EPSG", projectedGeographicCrs.getMapProjection()
				.getMethodIdentifiers().get(0).getName());
		assertEquals("9820", projectedGeographicCrs.getMapProjection()
				.getMethodIdentifiers().get(0).getUniqueIdentifier());
		assertEquals("Latitude of origin", projectedGeographicCrs
				.getMapProjection().getParameters().get(0).getName());
		assertEquals(52.0, projectedGeographicCrs.getMapProjection()
				.getParameters().get(0).getValue(), 0);
		assertEquals(UnitType.ANGLEUNIT, projectedGeographicCrs
				.getMapProjection().getParameters().get(0).getUnit().getType());
		assertEquals("degree", projectedGeographicCrs.getMapProjection()
				.getParameters().get(0).getUnit().getName());
		assertEquals(
				0.0174532925199433, projectedGeographicCrs.getMapProjection()
						.getParameters().get(0).getUnit().getConversionFactor(),
				0);
		assertEquals("Longitude of origin", projectedGeographicCrs
				.getMapProjection().getParameters().get(1).getName());
		assertEquals(10.0, projectedGeographicCrs.getMapProjection()
				.getParameters().get(1).getValue(), 0);
		assertEquals(UnitType.ANGLEUNIT, projectedGeographicCrs
				.getMapProjection().getParameters().get(1).getUnit().getType());
		assertEquals("degree", projectedGeographicCrs.getMapProjection()
				.getParameters().get(1).getUnit().getName());
		assertEquals(
				0.0174532925199433, projectedGeographicCrs.getMapProjection()
						.getParameters().get(1).getUnit().getConversionFactor(),
				0);
		assertEquals("False easting", projectedGeographicCrs.getMapProjection()
				.getParameters().get(2).getName());
		assertEquals(4321000.0, projectedGeographicCrs.getMapProjection()
				.getParameters().get(2).getValue(), 0);
		assertEquals(UnitType.LENGTHUNIT, projectedGeographicCrs
				.getMapProjection().getParameters().get(2).getUnit().getType());
		assertEquals("metre", projectedGeographicCrs.getMapProjection()
				.getParameters().get(2).getUnit().getName());
		assertEquals(1.0, projectedGeographicCrs.getMapProjection()
				.getParameters().get(2).getUnit().getConversionFactor(), 0);
		assertEquals("False northing", projectedGeographicCrs.getMapProjection()
				.getParameters().get(3).getName());
		assertEquals(3210000.0, projectedGeographicCrs.getMapProjection()
				.getParameters().get(3).getValue(), 0);
		assertEquals(UnitType.LENGTHUNIT, projectedGeographicCrs
				.getMapProjection().getParameters().get(3).getUnit().getType());
		assertEquals("metre", projectedGeographicCrs.getMapProjection()
				.getParameters().get(3).getUnit().getName());
		assertEquals(1.0, projectedGeographicCrs.getMapProjection()
				.getParameters().get(3).getUnit().getConversionFactor(), 0);
		assertEquals(CoordinateSystemType.CARTESIAN,
				projectedGeographicCrs.getCoordinateSystem().getType());
		assertEquals(2,
				projectedGeographicCrs.getCoordinateSystem().getDimension());
		assertEquals("Y", projectedGeographicCrs.getCoordinateSystem().getAxes()
				.get(0).getAbbreviation());
		assertEquals(AxisDirectionType.NORTH, projectedGeographicCrs
				.getCoordinateSystem().getAxes().get(0).getDirection());
		assertEquals(1, projectedGeographicCrs.getCoordinateSystem().getAxes()
				.get(0).getOrder().intValue());
		assertEquals("X", projectedGeographicCrs.getCoordinateSystem().getAxes()
				.get(1).getAbbreviation());
		assertEquals(AxisDirectionType.EAST, projectedGeographicCrs
				.getCoordinateSystem().getAxes().get(1).getDirection());
		assertEquals(2, projectedGeographicCrs.getCoordinateSystem().getAxes()
				.get(1).getOrder().intValue());
		assertEquals(UnitType.LENGTHUNIT, projectedGeographicCrs
				.getCoordinateSystem().getUnit().getType());
		assertEquals("metre", projectedGeographicCrs.getCoordinateSystem()
				.getUnit().getName());
		assertEquals(1.0, projectedGeographicCrs.getCoordinateSystem().getUnit()
				.getConversionFactor(), 0);
		assertEquals("Description of a purpose",
				projectedGeographicCrs.getUsages().get(0).getScope());
		assertEquals("An area description", projectedGeographicCrs.getUsages()
				.get(0).getExtent().getAreaDescription());
		assertEquals("EuroGeographics",
				projectedGeographicCrs.getIdentifiers().get(0).getName());
		assertEquals("ETRS-LAEA", projectedGeographicCrs.getIdentifiers().get(0)
				.getUniqueIdentifier());
		text = text.replaceAll("6378137", "6378137.0");
		assertEquals(text, projectedGeographicCrs.toString());
		assertEquals(text, CRSWriter.write(projectedGeographicCrs));
		assertEquals(WKTUtils.pretty(text),
				CRSWriter.writePretty(projectedGeographicCrs));

		text = "PROJCRS[\"NAD27 / Texas South Central\","
				+ "BASEGEOGCRS[\"NAD27\","
				+ "DATUM[\"North American Datum 1927\","
				+ "ELLIPSOID[\"Clarke 1866\",20925832.164,294.97869821,"
				+ "LENGTHUNIT[\"US survey foot\",0.304800609601219]]]],"
				+ "CONVERSION[\"Texas South Central SPCS27\","
				+ "METHOD[\"Lambert Conic Conformal (2SP)\",ID[\"EPSG\",9802]],"
				+ "PARAMETER[\"Latitude of false origin\",27.83333333333333,"
				+ "ANGLEUNIT[\"degree\",0.0174532925199433],ID[\"EPSG\",8821]],"
				+ "PARAMETER[\"Longitude of false origin\",-99.0,"
				+ "ANGLEUNIT[\"degree\",0.0174532925199433],ID[\"EPSG\",8822]],"
				+ "PARAMETER[\"Latitude of 1st standard parallel\",28.383333333333,"
				+ "ANGLEUNIT[\"degree\",0.0174532925199433],ID[\"EPSG\",8823]],"
				+ "PARAMETER[\"Latitude of 2nd standard parallel\",30.283333333333,"
				+ "ANGLEUNIT[\"degree\",0.0174532925199433],ID[\"EPSG\",8824]],"
				+ "PARAMETER[\"Easting at false origin\",2000000.0,"
				+ "LENGTHUNIT[\"US survey foot\",0.304800609601219],ID[\"EPSG\",8826]],"
				+ "PARAMETER[\"Northing at false origin\",0.0,"
				+ "LENGTHUNIT[\"US survey foot\",0.304800609601219],ID[\"EPSG\",8827]]],"
				+ "CS[Cartesian,2],AXIS[\"(X)\",east]," + "AXIS[\"(Y)\",north],"
				+ "LENGTHUNIT[\"US survey foot\",0.304800609601219],"
				+ "REMARK[\"Fundamental point: Meade's Ranch KS, latitude 39°13'26.686\"\"N,"
				+ "longitude 98°32'30.506\"\"W.\"]]";

		crs = CRSReader.read(text, true);
		projectedCrs = CRSReader.readProjected(text);
		assertEquals(crs, projectedCrs);
		projectedGeographicCrs = CRSReader.readProjectedGeographic(text);
		assertEquals(crs, projectedGeographicCrs);
		assertEquals(CoordinateReferenceSystemType.PROJECTED,
				projectedGeographicCrs.getType());
		assertEquals("NAD27 / Texas South Central",
				projectedGeographicCrs.getName());
		assertEquals(CoordinateReferenceSystemType.GEOGRAPHIC,
				projectedGeographicCrs.getBaseType());
		assertEquals("NAD27", projectedGeographicCrs.getBaseName());
		assertEquals("North American Datum 1927",
				projectedGeographicCrs.getGeodeticReferenceFrame().getName());
		assertEquals("Clarke 1866", projectedGeographicCrs
				.getGeodeticReferenceFrame().getEllipsoid().getName());
		assertEquals(20925832.164, projectedGeographicCrs
				.getGeodeticReferenceFrame().getEllipsoid().getSemiMajorAxis(),
				0);
		assertEquals(294.97869821,
				projectedGeographicCrs.getGeodeticReferenceFrame()
						.getEllipsoid().getInverseFlattening(),
				0);
		assertEquals(UnitType.LENGTHUNIT,
				projectedGeographicCrs.getGeodeticReferenceFrame()
						.getEllipsoid().getUnit().getType());
		assertEquals("US survey foot",
				projectedGeographicCrs.getGeodeticReferenceFrame()
						.getEllipsoid().getUnit().getName());
		assertEquals(0.304800609601219,
				projectedGeographicCrs.getGeodeticReferenceFrame()
						.getEllipsoid().getUnit().getConversionFactor(),
				0);
		assertEquals("Texas South Central SPCS27",
				projectedGeographicCrs.getMapProjection().getName());
		assertEquals("Lambert Conic Conformal (2SP)",
				projectedGeographicCrs.getMapProjection().getMethodName());
		assertEquals("EPSG", projectedGeographicCrs.getMapProjection()
				.getMethodIdentifiers().get(0).getName());
		assertEquals("9802", projectedGeographicCrs.getMapProjection()
				.getMethodIdentifiers().get(0).getUniqueIdentifier());
		assertEquals("Latitude of false origin", projectedGeographicCrs
				.getMapProjection().getParameters().get(0).getName());
		assertEquals(27.83333333333333, projectedGeographicCrs
				.getMapProjection().getParameters().get(0).getValue(), 0);
		assertEquals(UnitType.ANGLEUNIT, projectedGeographicCrs
				.getMapProjection().getParameters().get(0).getUnit().getType());
		assertEquals("degree", projectedGeographicCrs.getMapProjection()
				.getParameters().get(0).getUnit().getName());
		assertEquals(
				0.0174532925199433, projectedGeographicCrs.getMapProjection()
						.getParameters().get(0).getUnit().getConversionFactor(),
				0);
		assertEquals("EPSG", projectedGeographicCrs.getMapProjection()
				.getParameters().get(0).getIdentifiers().get(0).getName());
		assertEquals("8821",
				projectedGeographicCrs.getMapProjection().getParameters().get(0)
						.getIdentifiers().get(0).getUniqueIdentifier());
		assertEquals("Longitude of false origin", projectedGeographicCrs
				.getMapProjection().getParameters().get(1).getName());
		assertEquals(-99.0, projectedGeographicCrs.getMapProjection()
				.getParameters().get(1).getValue(), 0);
		assertEquals(UnitType.ANGLEUNIT, projectedGeographicCrs
				.getMapProjection().getParameters().get(1).getUnit().getType());
		assertEquals("degree", projectedGeographicCrs.getMapProjection()
				.getParameters().get(1).getUnit().getName());
		assertEquals(
				0.0174532925199433, projectedGeographicCrs.getMapProjection()
						.getParameters().get(1).getUnit().getConversionFactor(),
				0);
		assertEquals("EPSG", projectedGeographicCrs.getMapProjection()
				.getParameters().get(1).getIdentifiers().get(0).getName());
		assertEquals("8822",
				projectedGeographicCrs.getMapProjection().getParameters().get(1)
						.getIdentifiers().get(0).getUniqueIdentifier());
		assertEquals("Latitude of 1st standard parallel", projectedGeographicCrs
				.getMapProjection().getParameters().get(2).getName());
		assertEquals(28.383333333333, projectedGeographicCrs.getMapProjection()
				.getParameters().get(2).getValue(), 0);
		assertEquals(UnitType.ANGLEUNIT, projectedGeographicCrs
				.getMapProjection().getParameters().get(2).getUnit().getType());
		assertEquals("degree", projectedGeographicCrs.getMapProjection()
				.getParameters().get(2).getUnit().getName());
		assertEquals(
				0.0174532925199433, projectedGeographicCrs.getMapProjection()
						.getParameters().get(2).getUnit().getConversionFactor(),
				0);
		assertEquals("EPSG", projectedGeographicCrs.getMapProjection()
				.getParameters().get(2).getIdentifiers().get(0).getName());
		assertEquals("8823",
				projectedGeographicCrs.getMapProjection().getParameters().get(2)
						.getIdentifiers().get(0).getUniqueIdentifier());
		assertEquals("Latitude of 2nd standard parallel", projectedGeographicCrs
				.getMapProjection().getParameters().get(3).getName());
		assertEquals(30.283333333333, projectedGeographicCrs.getMapProjection()
				.getParameters().get(3).getValue(), 0);
		assertEquals(UnitType.ANGLEUNIT, projectedGeographicCrs
				.getMapProjection().getParameters().get(3).getUnit().getType());
		assertEquals("degree", projectedGeographicCrs.getMapProjection()
				.getParameters().get(3).getUnit().getName());
		assertEquals(
				0.0174532925199433, projectedGeographicCrs.getMapProjection()
						.getParameters().get(3).getUnit().getConversionFactor(),
				0);
		assertEquals("EPSG", projectedGeographicCrs.getMapProjection()
				.getParameters().get(3).getIdentifiers().get(0).getName());
		assertEquals("8824",
				projectedGeographicCrs.getMapProjection().getParameters().get(3)
						.getIdentifiers().get(0).getUniqueIdentifier());
		assertEquals("Easting at false origin", projectedGeographicCrs
				.getMapProjection().getParameters().get(4).getName());
		assertEquals(2000000.0, projectedGeographicCrs.getMapProjection()
				.getParameters().get(4).getValue(), 0);
		assertEquals(UnitType.LENGTHUNIT, projectedGeographicCrs
				.getMapProjection().getParameters().get(4).getUnit().getType());
		assertEquals("US survey foot", projectedGeographicCrs.getMapProjection()
				.getParameters().get(4).getUnit().getName());
		assertEquals(
				0.304800609601219, projectedGeographicCrs.getMapProjection()
						.getParameters().get(4).getUnit().getConversionFactor(),
				0);
		assertEquals("EPSG", projectedGeographicCrs.getMapProjection()
				.getParameters().get(4).getIdentifiers().get(0).getName());
		assertEquals("8826",
				projectedGeographicCrs.getMapProjection().getParameters().get(4)
						.getIdentifiers().get(0).getUniqueIdentifier());
		assertEquals("Northing at false origin", projectedGeographicCrs
				.getMapProjection().getParameters().get(5).getName());
		assertEquals(0.0, projectedGeographicCrs.getMapProjection()
				.getParameters().get(5).getValue(), 0);
		assertEquals(UnitType.LENGTHUNIT, projectedGeographicCrs
				.getMapProjection().getParameters().get(5).getUnit().getType());
		assertEquals("US survey foot", projectedGeographicCrs.getMapProjection()
				.getParameters().get(5).getUnit().getName());
		assertEquals(
				0.304800609601219, projectedGeographicCrs.getMapProjection()
						.getParameters().get(5).getUnit().getConversionFactor(),
				0);
		assertEquals("EPSG", projectedGeographicCrs.getMapProjection()
				.getParameters().get(5).getIdentifiers().get(0).getName());
		assertEquals("8827",
				projectedGeographicCrs.getMapProjection().getParameters().get(5)
						.getIdentifiers().get(0).getUniqueIdentifier());
		assertEquals(CoordinateSystemType.CARTESIAN,
				projectedGeographicCrs.getCoordinateSystem().getType());
		assertEquals(2,
				projectedGeographicCrs.getCoordinateSystem().getDimension());
		assertEquals("X", projectedGeographicCrs.getCoordinateSystem().getAxes()
				.get(0).getAbbreviation());
		assertEquals(AxisDirectionType.EAST, projectedGeographicCrs
				.getCoordinateSystem().getAxes().get(0).getDirection());
		assertEquals("Y", projectedGeographicCrs.getCoordinateSystem().getAxes()
				.get(1).getAbbreviation());
		assertEquals(AxisDirectionType.NORTH, projectedGeographicCrs
				.getCoordinateSystem().getAxes().get(1).getDirection());
		assertEquals(UnitType.LENGTHUNIT, projectedGeographicCrs
				.getCoordinateSystem().getUnit().getType());
		assertEquals("US survey foot", projectedGeographicCrs
				.getCoordinateSystem().getUnit().getName());
		assertEquals(0.304800609601219, projectedGeographicCrs
				.getCoordinateSystem().getUnit().getConversionFactor(), 0);
		assertEquals(
				"Fundamental point: Meade's Ranch KS, latitude 39°13'26.686\"N,longitude 98°32'30.506\"W.",
				projectedGeographicCrs.getRemark());
		text = text.replaceAll("20925832.164", "2.0925832164E7");
		assertEquals(text, projectedGeographicCrs.toString());
		assertEquals(text, CRSWriter.write(projectedGeographicCrs));
		assertEquals(WKTUtils.pretty(text),
				CRSWriter.writePretty(projectedGeographicCrs));

		text = "PROJCRS[\"NAD83 UTM 10\",BASEGEOGCRS[\"NAD83(86)\","
				+ "DATUM[\"North American Datum 1983\","
				+ "ELLIPSOID[\"GRS 1980\",6378137,298.257222101]],"
				+ "PRIMEM[\"Greenwich\",0]],CONVERSION[\"UTM zone 10N\","
				+ "METHOD[\"Transverse Mercator\"],"
				+ "PARAMETER[\"Latitude of natural origin\",0.0],"
				+ "PARAMETER[\"Longitude of natural origin\",-123.0],"
				+ "PARAMETER[\"Scale factor\",0.9996],"
				+ "PARAMETER[\"False easting\",500000.0],"
				+ "PARAMETER[\"False northing\",0.0],ID[\"EPSG\",16010]],"
				+ "CS[Cartesian,2]," + "AXIS[\"(E)\",east,ORDER[1]],"
				+ "AXIS[\"(N)\",north,ORDER[2]],LENGTHUNIT[\"metre\",1.0],"
				+ "REMARK[\"In this example parameter value units are not given. This is allowed for backward compatibility. However it is strongly recommended that units are explicitly given in the string, as in the previous two examples.\"]]";

		crs = CRSReader.read(text, true);
		projectedCrs = CRSReader.readProjected(text);
		assertEquals(crs, projectedCrs);
		projectedGeographicCrs = CRSReader.readProjectedGeographic(text);
		assertEquals(crs, projectedGeographicCrs);
		assertEquals(CoordinateReferenceSystemType.PROJECTED,
				projectedGeographicCrs.getType());
		assertEquals("NAD83 UTM 10", projectedGeographicCrs.getName());
		assertEquals(CoordinateReferenceSystemType.GEOGRAPHIC,
				projectedGeographicCrs.getBaseType());
		assertEquals("NAD83(86)", projectedGeographicCrs.getBaseName());
		assertEquals("North American Datum 1983",
				projectedGeographicCrs.getGeodeticReferenceFrame().getName());
		assertEquals("GRS 1980", projectedGeographicCrs
				.getGeodeticReferenceFrame().getEllipsoid().getName());
		assertEquals(6378137, projectedGeographicCrs.getGeodeticReferenceFrame()
				.getEllipsoid().getSemiMajorAxis(), 0);
		assertEquals(298.257222101,
				projectedGeographicCrs.getGeodeticReferenceFrame()
						.getEllipsoid().getInverseFlattening(),
				0);
		assertEquals("Greenwich", projectedGeographicCrs
				.getGeodeticReferenceFrame().getPrimeMeridian().getName());
		assertEquals(0, projectedGeographicCrs.getGeodeticReferenceFrame()
				.getPrimeMeridian().getIrmLongitude(), 0);
		assertEquals("UTM zone 10N",
				projectedGeographicCrs.getMapProjection().getName());
		assertEquals("Transverse Mercator",
				projectedGeographicCrs.getMapProjection().getMethodName());
		assertEquals("Latitude of natural origin", projectedGeographicCrs
				.getMapProjection().getParameters().get(0).getName());
		assertEquals(0.0, projectedGeographicCrs.getMapProjection()
				.getParameters().get(0).getValue(), 0);
		assertEquals("Longitude of natural origin", projectedGeographicCrs
				.getMapProjection().getParameters().get(1).getName());
		assertEquals(-123.0, projectedGeographicCrs.getMapProjection()
				.getParameters().get(1).getValue(), 0);
		assertEquals("Scale factor", projectedGeographicCrs.getMapProjection()
				.getParameters().get(2).getName());
		assertEquals(0.9996, projectedGeographicCrs.getMapProjection()
				.getParameters().get(2).getValue(), 0);
		assertEquals("False easting", projectedGeographicCrs.getMapProjection()
				.getParameters().get(3).getName());
		assertEquals(500000.0, projectedGeographicCrs.getMapProjection()
				.getParameters().get(3).getValue(), 0);
		assertEquals("False northing", projectedGeographicCrs.getMapProjection()
				.getParameters().get(4).getName());
		assertEquals(0.0, projectedGeographicCrs.getMapProjection()
				.getParameters().get(4).getValue(), 0);
		assertEquals("EPSG", projectedGeographicCrs.getMapProjection()
				.getIdentifiers().get(0).getName());
		assertEquals("16010", projectedGeographicCrs.getMapProjection()
				.getIdentifiers().get(0).getUniqueIdentifier());
		assertEquals(CoordinateSystemType.CARTESIAN,
				projectedGeographicCrs.getCoordinateSystem().getType());
		assertEquals(2,
				projectedGeographicCrs.getCoordinateSystem().getDimension());
		assertEquals("E", projectedGeographicCrs.getCoordinateSystem().getAxes()
				.get(0).getAbbreviation());
		assertEquals(AxisDirectionType.EAST, projectedGeographicCrs
				.getCoordinateSystem().getAxes().get(0).getDirection());
		assertEquals(1, projectedGeographicCrs.getCoordinateSystem().getAxes()
				.get(0).getOrder().intValue());
		assertEquals("N", projectedGeographicCrs.getCoordinateSystem().getAxes()
				.get(1).getAbbreviation());
		assertEquals(AxisDirectionType.NORTH, projectedGeographicCrs
				.getCoordinateSystem().getAxes().get(1).getDirection());
		assertEquals(2, projectedGeographicCrs.getCoordinateSystem().getAxes()
				.get(1).getOrder().intValue());
		assertEquals(UnitType.LENGTHUNIT, projectedGeographicCrs
				.getCoordinateSystem().getUnit().getType());
		assertEquals("metre", projectedGeographicCrs.getCoordinateSystem()
				.getUnit().getName());
		assertEquals(1.0, projectedGeographicCrs.getCoordinateSystem().getUnit()
				.getConversionFactor(), 0);
		assertEquals(
				"In this example parameter value units are not given. This is allowed for backward compatibility. However it is strongly recommended that units are explicitly given in the string, as in the previous two examples.",
				projectedGeographicCrs.getRemark());
		text = text.replaceAll("6378137", "6378137.0").replace(",0]", ",0.0]");
		assertEquals(text, projectedGeographicCrs.toString());
		assertEquals(text, CRSWriter.write(projectedGeographicCrs));
		assertEquals(WKTUtils.pretty(text),
				CRSWriter.writePretty(projectedGeographicCrs));

		text = "PROJCRS[\"WGS 84 (G1762) / UTM zone 31N 3D\",BASEGEOGCRS[\"WGS 84\","
				+ "DATUM[\"World Geodetic System of 1984 (G1762)\","
				+ "ELLIPSOID[\"WGS 84\",6378137,298.257223563,LENGTHUNIT[\"metre\",1.0]]]],"
				+ "CONVERSION[\"UTM zone 31N 3D\","
				+ "METHOD[\"Transverse Mercator (3D)\"],"
				+ "PARAMETER[\"Latitude of origin\",0.0,"
				+ "ANGLEUNIT[\"degree\",0.0174532925199433]],"
				+ "PARAMETER[\"Longitude of origin\",3.0,"
				+ "ANGLEUNIT[\"degree\",0.0174532925199433]],"
				+ "PARAMETER[\"Scale factor\",0.9996,SCALEUNIT[\"unity\",1.0]],"
				+ "PARAMETER[\"False easting\",500000.0,LENGTHUNIT[\"metre\",1.0]],"
				+ "PARAMETER[\"False northing\",0.0,LENGTHUNIT[\"metre\",1.0]]],"
				+ "CS[Cartesian,3],AXIS[\"(E)\",east,ORDER[1]],"
				+ "AXIS[\"(N)\",north,ORDER[2]],"
				+ "AXIS[\"ellipsoidal height (h)\",up,ORDER[3]],"
				+ "LENGTHUNIT[\"metre\",1.0]]";

		crs = CRSReader.read(text, true);
		projectedCrs = CRSReader.readProjected(text);
		assertEquals(crs, projectedCrs);
		projectedGeographicCrs = CRSReader.readProjectedGeographic(text);
		assertEquals(crs, projectedGeographicCrs);
		assertEquals(CoordinateReferenceSystemType.PROJECTED,
				projectedGeographicCrs.getType());
		assertEquals("WGS 84 (G1762) / UTM zone 31N 3D",
				projectedGeographicCrs.getName());
		assertEquals(CoordinateReferenceSystemType.GEOGRAPHIC,
				projectedGeographicCrs.getBaseType());
		assertEquals("WGS 84", projectedGeographicCrs.getBaseName());
		assertEquals("World Geodetic System of 1984 (G1762)",
				projectedGeographicCrs.getGeodeticReferenceFrame().getName());
		assertEquals("WGS 84", projectedGeographicCrs
				.getGeodeticReferenceFrame().getEllipsoid().getName());
		assertEquals(6378137, projectedGeographicCrs.getGeodeticReferenceFrame()
				.getEllipsoid().getSemiMajorAxis(), 0);
		assertEquals(298.257223563,
				projectedGeographicCrs.getGeodeticReferenceFrame()
						.getEllipsoid().getInverseFlattening(),
				0);
		assertEquals(UnitType.LENGTHUNIT,
				projectedGeographicCrs.getGeodeticReferenceFrame()
						.getEllipsoid().getUnit().getType());
		assertEquals("metre", projectedGeographicCrs.getGeodeticReferenceFrame()
				.getEllipsoid().getUnit().getName());
		assertEquals(1.0, projectedGeographicCrs.getGeodeticReferenceFrame()
				.getEllipsoid().getUnit().getConversionFactor(), 0);
		assertEquals("UTM zone 31N 3D",
				projectedGeographicCrs.getMapProjection().getName());
		assertEquals("Transverse Mercator (3D)",
				projectedGeographicCrs.getMapProjection().getMethodName());
		assertEquals("Latitude of origin", projectedGeographicCrs
				.getMapProjection().getParameters().get(0).getName());
		assertEquals(0.0, projectedGeographicCrs.getMapProjection()
				.getParameters().get(0).getValue(), 0);
		assertEquals(UnitType.ANGLEUNIT, projectedGeographicCrs
				.getMapProjection().getParameters().get(0).getUnit().getType());
		assertEquals("degree", projectedGeographicCrs.getMapProjection()
				.getParameters().get(0).getUnit().getName());
		assertEquals(
				0.0174532925199433, projectedGeographicCrs.getMapProjection()
						.getParameters().get(0).getUnit().getConversionFactor(),
				0);
		assertEquals("Longitude of origin", projectedGeographicCrs
				.getMapProjection().getParameters().get(1).getName());
		assertEquals(3.0, projectedGeographicCrs.getMapProjection()
				.getParameters().get(1).getValue(), 0);
		assertEquals(UnitType.ANGLEUNIT, projectedGeographicCrs
				.getMapProjection().getParameters().get(1).getUnit().getType());
		assertEquals("degree", projectedGeographicCrs.getMapProjection()
				.getParameters().get(1).getUnit().getName());
		assertEquals(
				0.0174532925199433, projectedGeographicCrs.getMapProjection()
						.getParameters().get(1).getUnit().getConversionFactor(),
				0);
		assertEquals("Scale factor", projectedGeographicCrs.getMapProjection()
				.getParameters().get(2).getName());
		assertEquals(0.9996, projectedGeographicCrs.getMapProjection()
				.getParameters().get(2).getValue(), 0);
		assertEquals(UnitType.SCALEUNIT, projectedGeographicCrs
				.getMapProjection().getParameters().get(2).getUnit().getType());
		assertEquals("unity", projectedGeographicCrs.getMapProjection()
				.getParameters().get(2).getUnit().getName());
		assertEquals(1.0, projectedGeographicCrs.getMapProjection()
				.getParameters().get(2).getUnit().getConversionFactor(), 0);
		assertEquals("False easting", projectedGeographicCrs.getMapProjection()
				.getParameters().get(3).getName());
		assertEquals(500000.0, projectedGeographicCrs.getMapProjection()
				.getParameters().get(3).getValue(), 0);
		assertEquals(UnitType.LENGTHUNIT, projectedGeographicCrs
				.getMapProjection().getParameters().get(3).getUnit().getType());
		assertEquals("metre", projectedGeographicCrs.getMapProjection()
				.getParameters().get(3).getUnit().getName());
		assertEquals(1.0, projectedGeographicCrs.getMapProjection()
				.getParameters().get(3).getUnit().getConversionFactor(), 0);
		assertEquals("False northing", projectedGeographicCrs.getMapProjection()
				.getParameters().get(4).getName());
		assertEquals(0.0, projectedGeographicCrs.getMapProjection()
				.getParameters().get(4).getValue(), 0);
		assertEquals(UnitType.LENGTHUNIT, projectedGeographicCrs
				.getMapProjection().getParameters().get(4).getUnit().getType());
		assertEquals("metre", projectedGeographicCrs.getMapProjection()
				.getParameters().get(4).getUnit().getName());
		assertEquals(1.0, projectedGeographicCrs.getMapProjection()
				.getParameters().get(4).getUnit().getConversionFactor(), 0);
		assertEquals(CoordinateSystemType.CARTESIAN,
				projectedGeographicCrs.getCoordinateSystem().getType());
		assertEquals(3,
				projectedGeographicCrs.getCoordinateSystem().getDimension());
		assertEquals("E", projectedGeographicCrs.getCoordinateSystem().getAxes()
				.get(0).getAbbreviation());
		assertEquals(AxisDirectionType.EAST, projectedGeographicCrs
				.getCoordinateSystem().getAxes().get(0).getDirection());
		assertEquals(1, projectedGeographicCrs.getCoordinateSystem().getAxes()
				.get(0).getOrder().intValue());
		assertEquals("N", projectedGeographicCrs.getCoordinateSystem().getAxes()
				.get(1).getAbbreviation());
		assertEquals(AxisDirectionType.NORTH, projectedGeographicCrs
				.getCoordinateSystem().getAxes().get(1).getDirection());
		assertEquals(2, projectedGeographicCrs.getCoordinateSystem().getAxes()
				.get(1).getOrder().intValue());
		assertEquals("ellipsoidal height", projectedGeographicCrs
				.getCoordinateSystem().getAxes().get(2).getName());
		assertEquals("h", projectedGeographicCrs.getCoordinateSystem().getAxes()
				.get(2).getAbbreviation());
		assertEquals(AxisDirectionType.UP, projectedGeographicCrs
				.getCoordinateSystem().getAxes().get(2).getDirection());
		assertEquals(3, projectedGeographicCrs.getCoordinateSystem().getAxes()
				.get(2).getOrder().intValue());
		assertEquals(UnitType.LENGTHUNIT, projectedGeographicCrs
				.getCoordinateSystem().getUnit().getType());
		assertEquals("metre", projectedGeographicCrs.getCoordinateSystem()
				.getUnit().getName());
		assertEquals(1.0, projectedGeographicCrs.getCoordinateSystem().getUnit()
				.getConversionFactor(), 0);
		text = text.replaceAll("6378137", "6378137.0");
		assertEquals(text, projectedGeographicCrs.toString());
		assertEquals(text, CRSWriter.write(projectedGeographicCrs));
		assertEquals(WKTUtils.pretty(text),
				CRSWriter.writePretty(projectedGeographicCrs));

	}

	/**
	 * Test vertical reference frame
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testVerticalReferenceFrame() throws IOException {

		String text = "VDATUM[\"Newlyn\"]";
		CRSReader reader = new CRSReader(text);
		VerticalReferenceFrame verticalReferenceFrame = reader
				.readVerticalReferenceFrame();
		assertEquals("Newlyn", verticalReferenceFrame.getName());
		reader.close();
		assertEquals(text, verticalReferenceFrame.toString());

		text = "VERTICALDATUM[\"Newlyn\",ANCHOR[\"Mean Sea Level 1915 to 1921.\"]]";
		reader = new CRSReader(text);
		verticalReferenceFrame = reader.readVerticalReferenceFrame();
		assertEquals("Newlyn", verticalReferenceFrame.getName());
		assertEquals("Mean Sea Level 1915 to 1921.",
				verticalReferenceFrame.getAnchor());
		reader.close();
		assertEquals(text.replaceAll("VERTICALDATUM", "VDATUM"),
				verticalReferenceFrame.toString());

	}

	/**
	 * Test vertical coordinate reference system
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testVerticalCoordinateReferenceSystem() throws IOException {

		String text = "VERTCRS[\"NAVD88\","
				+ "VDATUM[\"North American Vertical Datum 1988\"],"
				+ "CS[vertical,1],AXIS[\"gravity-related height (H)\",up],"
				+ "LENGTHUNIT[\"metre\",1.0]]";

		CoordinateReferenceSystem crs = CRSReader.read(text, true);
		VerticalCoordinateReferenceSystem verticalCrs = CRSReader
				.readVertical(text);
		assertEquals(crs, verticalCrs);
		assertEquals(CoordinateReferenceSystemType.VERTICAL,
				verticalCrs.getType());
		assertEquals("NAVD88", verticalCrs.getName());
		assertEquals("North American Vertical Datum 1988",
				verticalCrs.getVerticalReferenceFrame().getName());
		assertEquals(CoordinateSystemType.VERTICAL,
				verticalCrs.getCoordinateSystem().getType());
		assertEquals(1, verticalCrs.getCoordinateSystem().getDimension());
		assertEquals("gravity-related height",
				verticalCrs.getCoordinateSystem().getAxes().get(0).getName());
		assertEquals("H", verticalCrs.getCoordinateSystem().getAxes().get(0)
				.getAbbreviation());
		assertEquals(AxisDirectionType.UP, verticalCrs.getCoordinateSystem()
				.getAxes().get(0).getDirection());
		assertEquals(UnitType.LENGTHUNIT,
				verticalCrs.getCoordinateSystem().getUnit().getType());
		assertEquals("metre",
				verticalCrs.getCoordinateSystem().getUnit().getName());
		assertEquals(1.0, verticalCrs.getCoordinateSystem().getUnit()
				.getConversionFactor(), 0);
		assertEquals(text, verticalCrs.toString());
		assertEquals(text, CRSWriter.write(verticalCrs));
		assertEquals(WKTUtils.pretty(text), CRSWriter.writePretty(verticalCrs));

		text = "VERTCRS[\"CGVD2013\","
				+ "VRF[\"Canadian Geodetic Vertical Datum of 2013\"],"
				+ "CS[vertical,1],AXIS[\"gravity-related height (H)\",up],"
				+ "LENGTHUNIT[\"metre\",1.0],"
				+ "GEOIDMODEL[\"CGG2013\",ID[\"EPSG\",6648]]]";

		crs = CRSReader.read(text, true);
		verticalCrs = CRSReader.readVertical(text);
		assertEquals(crs, verticalCrs);
		assertEquals(CoordinateReferenceSystemType.VERTICAL,
				verticalCrs.getType());
		assertEquals("CGVD2013", verticalCrs.getName());
		assertEquals("Canadian Geodetic Vertical Datum of 2013",
				verticalCrs.getVerticalReferenceFrame().getName());
		assertEquals(CoordinateSystemType.VERTICAL,
				verticalCrs.getCoordinateSystem().getType());
		assertEquals(1, verticalCrs.getCoordinateSystem().getDimension());
		assertEquals("gravity-related height",
				verticalCrs.getCoordinateSystem().getAxes().get(0).getName());
		assertEquals("H", verticalCrs.getCoordinateSystem().getAxes().get(0)
				.getAbbreviation());
		assertEquals(AxisDirectionType.UP, verticalCrs.getCoordinateSystem()
				.getAxes().get(0).getDirection());
		assertEquals(UnitType.LENGTHUNIT,
				verticalCrs.getCoordinateSystem().getUnit().getType());
		assertEquals("metre",
				verticalCrs.getCoordinateSystem().getUnit().getName());
		assertEquals(1.0, verticalCrs.getCoordinateSystem().getUnit()
				.getConversionFactor(), 0);
		assertEquals("CGG2013", verticalCrs.getGeoidModelName());
		assertEquals("EPSG", verticalCrs.getGeoidModelIdentifier().getName());
		assertEquals("6648",
				verticalCrs.getGeoidModelIdentifier().getUniqueIdentifier());
		text = text.replaceAll("VRF", "VDATUM");
		assertEquals(text, verticalCrs.toString());
		assertEquals(text, CRSWriter.write(verticalCrs));
		assertEquals(WKTUtils.pretty(text), CRSWriter.writePretty(verticalCrs));

		text = "VERTCRS[\"RH2000\","
				+ "DYNAMIC[FRAMEEPOCH[2000.0],MODEL[\"NKG2016LU\"]],"
				+ "VDATUM[\"Rikets Hojdsystem 2000\"],CS[vertical,1],"
				+ "AXIS[\"gravity-related height (H)\",up],"
				+ "LENGTHUNIT[\"metre\",1.0]]";

		crs = CRSReader.read(text, true);
		verticalCrs = CRSReader.readVertical(text);
		assertEquals(crs, verticalCrs);
		assertEquals(CoordinateReferenceSystemType.VERTICAL,
				verticalCrs.getType());
		assertEquals("RH2000", verticalCrs.getName());
		assertEquals(2000.0, verticalCrs.getDynamic().getReferenceEpoch(), 0);
		assertEquals("NKG2016LU",
				verticalCrs.getDynamic().getDeformationModelName());
		assertEquals("Rikets Hojdsystem 2000",
				verticalCrs.getVerticalReferenceFrame().getName());
		assertEquals(CoordinateSystemType.VERTICAL,
				verticalCrs.getCoordinateSystem().getType());
		assertEquals(1, verticalCrs.getCoordinateSystem().getDimension());
		assertEquals("gravity-related height",
				verticalCrs.getCoordinateSystem().getAxes().get(0).getName());
		assertEquals("H", verticalCrs.getCoordinateSystem().getAxes().get(0)
				.getAbbreviation());
		assertEquals(AxisDirectionType.UP, verticalCrs.getCoordinateSystem()
				.getAxes().get(0).getDirection());
		assertEquals(UnitType.LENGTHUNIT,
				verticalCrs.getCoordinateSystem().getUnit().getType());
		assertEquals("metre",
				verticalCrs.getCoordinateSystem().getUnit().getName());
		assertEquals(1.0, verticalCrs.getCoordinateSystem().getUnit()
				.getConversionFactor(), 0);
		assertEquals(text, verticalCrs.toString());
		assertEquals(text, CRSWriter.write(verticalCrs));
		assertEquals(WKTUtils.pretty(text), CRSWriter.writePretty(verticalCrs));

	}

	/**
	 * Test engineering coordinate reference system
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testEngineeringCoordinateReferenceSystem() throws IOException {

		String text = "ENGCRS[\"A construction site CRS\","
				+ "EDATUM[\"P1\",ANCHOR[\"Peg in south corner\"]],"
				+ "CS[Cartesian,2],AXIS[\"site east\",southWest,ORDER[1]],"
				+ "AXIS[\"site north\",southEast,ORDER[2]],"
				+ "LENGTHUNIT[\"metre\",1.0],"
				+ "USAGE[SCOPE[\"Construction\"],TIMEEXTENT[\"date/time t1\",\"date/time t2\"]]]";

		CoordinateReferenceSystem crs = CRSReader.read(text, true);
		EngineeringCoordinateReferenceSystem engineeringCrs = CRSReader
				.readEngineering(text);
		assertEquals(crs, engineeringCrs);
		assertEquals(CoordinateReferenceSystemType.ENGINEERING,
				engineeringCrs.getType());
		assertEquals("A construction site CRS", engineeringCrs.getName());
		assertEquals("P1", engineeringCrs.getEngineeringDatum().getName());
		assertEquals("Peg in south corner",
				engineeringCrs.getEngineeringDatum().getAnchor());
		assertEquals(CoordinateSystemType.CARTESIAN,
				engineeringCrs.getCoordinateSystem().getType());
		assertEquals(2, engineeringCrs.getCoordinateSystem().getDimension());
		assertEquals("site east", engineeringCrs.getCoordinateSystem().getAxes()
				.get(0).getName());
		assertEquals(AxisDirectionType.SOUTH_WEST, engineeringCrs
				.getCoordinateSystem().getAxes().get(0).getDirection());
		assertEquals(1, engineeringCrs.getCoordinateSystem().getAxes().get(0)
				.getOrder().intValue());
		assertEquals("site north", engineeringCrs.getCoordinateSystem()
				.getAxes().get(1).getName());
		assertEquals(AxisDirectionType.SOUTH_EAST, engineeringCrs
				.getCoordinateSystem().getAxes().get(1).getDirection());
		assertEquals(2, engineeringCrs.getCoordinateSystem().getAxes().get(1)
				.getOrder().intValue());
		assertEquals(UnitType.LENGTHUNIT,
				engineeringCrs.getCoordinateSystem().getUnit().getType());
		assertEquals("metre",
				engineeringCrs.getCoordinateSystem().getUnit().getName());
		assertEquals(1.0, engineeringCrs.getCoordinateSystem().getUnit()
				.getConversionFactor(), 0);
		assertEquals(text, engineeringCrs.toString());
		assertEquals(text, CRSWriter.write(engineeringCrs));
		assertEquals(WKTUtils.pretty(text),
				CRSWriter.writePretty(engineeringCrs));

		text = "ENGINEERINGCRS[\"Astra Minas Grid\","
				+ "ENGINEERINGDATUM[\"Astra Minas\"],CS[Cartesian,2],"
				+ "AXIS[\"northing (X)\",north,ORDER[1]],"
				+ "AXIS[\"westing (Y)\",west,ORDER[2]],"
				+ "LENGTHUNIT[\"metre\",1.0],ID[\"EPSG\",5800]]";

		crs = CRSReader.read(text, true);
		engineeringCrs = CRSReader.readEngineering(text);
		assertEquals(crs, engineeringCrs);
		assertEquals(CoordinateReferenceSystemType.ENGINEERING,
				engineeringCrs.getType());
		assertEquals("Astra Minas Grid", engineeringCrs.getName());
		assertEquals("Astra Minas",
				engineeringCrs.getEngineeringDatum().getName());
		assertEquals(CoordinateSystemType.CARTESIAN,
				engineeringCrs.getCoordinateSystem().getType());
		assertEquals(2, engineeringCrs.getCoordinateSystem().getDimension());
		assertEquals("northing", engineeringCrs.getCoordinateSystem().getAxes()
				.get(0).getName());
		assertEquals("X", engineeringCrs.getCoordinateSystem().getAxes().get(0)
				.getAbbreviation());
		assertEquals(AxisDirectionType.NORTH, engineeringCrs
				.getCoordinateSystem().getAxes().get(0).getDirection());
		assertEquals(1, engineeringCrs.getCoordinateSystem().getAxes().get(0)
				.getOrder().intValue());
		assertEquals("westing", engineeringCrs.getCoordinateSystem().getAxes()
				.get(1).getName());
		assertEquals("Y", engineeringCrs.getCoordinateSystem().getAxes().get(1)
				.getAbbreviation());
		assertEquals(AxisDirectionType.WEST, engineeringCrs
				.getCoordinateSystem().getAxes().get(1).getDirection());
		assertEquals(2, engineeringCrs.getCoordinateSystem().getAxes().get(1)
				.getOrder().intValue());
		assertEquals(UnitType.LENGTHUNIT,
				engineeringCrs.getCoordinateSystem().getUnit().getType());
		assertEquals("metre",
				engineeringCrs.getCoordinateSystem().getUnit().getName());
		assertEquals(1.0, engineeringCrs.getCoordinateSystem().getUnit()
				.getConversionFactor(), 0);
		assertEquals("EPSG", engineeringCrs.getIdentifiers().get(0).getName());
		assertEquals("5800",
				engineeringCrs.getIdentifiers().get(0).getUniqueIdentifier());
		text = text.replaceAll("ENGINEERINGCRS", "ENGCRS")
				.replaceAll("ENGINEERINGDATUM", "EDATUM");
		assertEquals(text, engineeringCrs.toString());
		assertEquals(text, CRSWriter.write(engineeringCrs));
		assertEquals(WKTUtils.pretty(text),
				CRSWriter.writePretty(engineeringCrs));

		text = "ENGCRS[\"A ship-centred CRS\","
				+ "EDATUM[\"Ship reference point\",ANCHOR[\"Centre of buoyancy\"]],"
				+ "CS[Cartesian,3],AXIS[\"(x)\",forward],"
				+ "AXIS[\"(y)\",starboard],AXIS[\"(z)\",down],"
				+ "LENGTHUNIT[\"metre\",1.0]]";

		crs = CRSReader.read(text, true);
		engineeringCrs = CRSReader.readEngineering(text);
		assertEquals(crs, engineeringCrs);
		assertEquals(CoordinateReferenceSystemType.ENGINEERING,
				engineeringCrs.getType());
		assertEquals("A ship-centred CRS", engineeringCrs.getName());
		assertEquals("Ship reference point",
				engineeringCrs.getEngineeringDatum().getName());
		assertEquals("Centre of buoyancy",
				engineeringCrs.getEngineeringDatum().getAnchor());
		assertEquals(CoordinateSystemType.CARTESIAN,
				engineeringCrs.getCoordinateSystem().getType());
		assertEquals(3, engineeringCrs.getCoordinateSystem().getDimension());
		assertEquals("x", engineeringCrs.getCoordinateSystem().getAxes().get(0)
				.getAbbreviation());
		assertEquals(AxisDirectionType.FORWARD, engineeringCrs
				.getCoordinateSystem().getAxes().get(0).getDirection());
		assertEquals("y", engineeringCrs.getCoordinateSystem().getAxes().get(1)
				.getAbbreviation());
		assertEquals(AxisDirectionType.STARBOARD, engineeringCrs
				.getCoordinateSystem().getAxes().get(1).getDirection());
		assertEquals("z", engineeringCrs.getCoordinateSystem().getAxes().get(2)
				.getAbbreviation());
		assertEquals(AxisDirectionType.DOWN, engineeringCrs
				.getCoordinateSystem().getAxes().get(2).getDirection());
		assertEquals(UnitType.LENGTHUNIT,
				engineeringCrs.getCoordinateSystem().getUnit().getType());
		assertEquals("metre",
				engineeringCrs.getCoordinateSystem().getUnit().getName());
		assertEquals(1.0, engineeringCrs.getCoordinateSystem().getUnit()
				.getConversionFactor(), 0);
		assertEquals(text, engineeringCrs.toString());
		assertEquals(text, CRSWriter.write(engineeringCrs));
		assertEquals(WKTUtils.pretty(text),
				CRSWriter.writePretty(engineeringCrs));

		text = "ENGCRS[\"An analogue image CRS\","
				+ "EDATUM[\"Image reference point\","
				+ "ANCHOR[\"Top left corner of image = 0,0\"]],"
				+ "CS[Cartesian,2],AXIS[\"Column (x)\",columnPositive],"
				+ "AXIS[\"Row (y)\",rowPositive],"
				+ "LENGTHUNIT[\"micrometre\",1E-6]]";

		crs = CRSReader.read(text, true);
		engineeringCrs = CRSReader.readEngineering(text);
		assertEquals(crs, engineeringCrs);
		assertEquals(CoordinateReferenceSystemType.ENGINEERING,
				engineeringCrs.getType());
		assertEquals("An analogue image CRS", engineeringCrs.getName());
		assertEquals("Image reference point",
				engineeringCrs.getEngineeringDatum().getName());
		assertEquals("Top left corner of image = 0,0",
				engineeringCrs.getEngineeringDatum().getAnchor());
		assertEquals(CoordinateSystemType.CARTESIAN,
				engineeringCrs.getCoordinateSystem().getType());
		assertEquals(2, engineeringCrs.getCoordinateSystem().getDimension());
		assertEquals("Column", engineeringCrs.getCoordinateSystem().getAxes()
				.get(0).getName());
		assertEquals("x", engineeringCrs.getCoordinateSystem().getAxes().get(0)
				.getAbbreviation());
		assertEquals(AxisDirectionType.COLUMN_POSITIVE, engineeringCrs
				.getCoordinateSystem().getAxes().get(0).getDirection());
		assertEquals("Row", engineeringCrs.getCoordinateSystem().getAxes()
				.get(1).getName());
		assertEquals("y", engineeringCrs.getCoordinateSystem().getAxes().get(1)
				.getAbbreviation());
		assertEquals(AxisDirectionType.ROW_POSITIVE, engineeringCrs
				.getCoordinateSystem().getAxes().get(1).getDirection());
		assertEquals(UnitType.LENGTHUNIT,
				engineeringCrs.getCoordinateSystem().getUnit().getType());
		assertEquals("micrometre",
				engineeringCrs.getCoordinateSystem().getUnit().getName());
		assertEquals(1E-6, engineeringCrs.getCoordinateSystem().getUnit()
				.getConversionFactor(), 0);
		text = text.replaceAll("1E-6", "1.0E-6");
		assertEquals(text, engineeringCrs.toString());
		assertEquals(text, CRSWriter.write(engineeringCrs));
		assertEquals(WKTUtils.pretty(text),
				CRSWriter.writePretty(engineeringCrs));

		text = "ENGCRS[\"A digital image CRS\","
				+ "EDATUM[\"Image reference point\","
				+ "ANCHOR[\"Top left corner of image = 0,0\"]],"
				+ "CS[ordinal,2],"
				+ "AXIS[\"Column pixel (x)\",columnPositive,ORDER[1]],"
				+ "AXIS[\"Row pixel (y)\",rowPositive,ORDER[2]]]";

		crs = CRSReader.read(text, true);
		engineeringCrs = CRSReader.readEngineering(text);
		assertEquals(crs, engineeringCrs);
		assertEquals(CoordinateReferenceSystemType.ENGINEERING,
				engineeringCrs.getType());
		assertEquals("A digital image CRS", engineeringCrs.getName());
		assertEquals("Image reference point",
				engineeringCrs.getEngineeringDatum().getName());
		assertEquals("Top left corner of image = 0,0",
				engineeringCrs.getEngineeringDatum().getAnchor());
		assertEquals(CoordinateSystemType.ORDINAL,
				engineeringCrs.getCoordinateSystem().getType());
		assertEquals(2, engineeringCrs.getCoordinateSystem().getDimension());
		assertEquals("Column pixel", engineeringCrs.getCoordinateSystem()
				.getAxes().get(0).getName());
		assertEquals("x", engineeringCrs.getCoordinateSystem().getAxes().get(0)
				.getAbbreviation());
		assertEquals(AxisDirectionType.COLUMN_POSITIVE, engineeringCrs
				.getCoordinateSystem().getAxes().get(0).getDirection());
		assertEquals(1, engineeringCrs.getCoordinateSystem().getAxes().get(0)
				.getOrder().intValue());
		assertEquals("Row pixel", engineeringCrs.getCoordinateSystem().getAxes()
				.get(1).getName());
		assertEquals("y", engineeringCrs.getCoordinateSystem().getAxes().get(1)
				.getAbbreviation());
		assertEquals(AxisDirectionType.ROW_POSITIVE, engineeringCrs
				.getCoordinateSystem().getAxes().get(1).getDirection());
		assertEquals(2, engineeringCrs.getCoordinateSystem().getAxes().get(1)
				.getOrder().intValue());
		assertEquals(text, engineeringCrs.toString());
		assertEquals(text, CRSWriter.write(engineeringCrs));
		assertEquals(WKTUtils.pretty(text),
				CRSWriter.writePretty(engineeringCrs));

	}

	/**
	 * Test parametric coordinate reference system
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testParametricCoordinateReferenceSystem() throws IOException {

		String text = "PARAMETRICCRS[\"WMO standard atmosphere layer 0\","
				+ "PDATUM[\"Mean Sea Level\",ANCHOR[\"1013.25 hPa at 15°C\"]],"
				+ "CS[parametric,1],"
				+ "AXIS[\"pressure (hPa)\",up],PARAMETRICUNIT[\"HectoPascal\",100.0]]";

		CoordinateReferenceSystem crs = CRSReader.read(text, true);
		ParametricCoordinateReferenceSystem parametricCrs = CRSReader
				.readParametric(text);
		assertEquals(crs, parametricCrs);
		assertEquals(CoordinateReferenceSystemType.PARAMETRIC,
				parametricCrs.getType());
		assertEquals("WMO standard atmosphere layer 0",
				parametricCrs.getName());
		assertEquals("Mean Sea Level",
				parametricCrs.getParametricDatum().getName());
		assertEquals("1013.25 hPa at 15°C",
				parametricCrs.getParametricDatum().getAnchor());
		assertEquals(CoordinateSystemType.PARAMETRIC,
				parametricCrs.getCoordinateSystem().getType());
		assertEquals(1, parametricCrs.getCoordinateSystem().getDimension());
		assertEquals("pressure",
				parametricCrs.getCoordinateSystem().getAxes().get(0).getName());
		assertEquals("hPa", parametricCrs.getCoordinateSystem().getAxes().get(0)
				.getAbbreviation());
		assertEquals(AxisDirectionType.UP, parametricCrs.getCoordinateSystem()
				.getAxes().get(0).getDirection());
		assertEquals(UnitType.PARAMETRICUNIT,
				parametricCrs.getCoordinateSystem().getUnit().getType());
		assertEquals("HectoPascal",
				parametricCrs.getCoordinateSystem().getUnit().getName());
		assertEquals(100.0, parametricCrs.getCoordinateSystem().getUnit()
				.getConversionFactor(), 0);
		assertEquals(text, parametricCrs.toString());
		assertEquals(text, CRSWriter.write(parametricCrs));
		assertEquals(WKTUtils.pretty(text),
				CRSWriter.writePretty(parametricCrs));

	}

	/**
	 * Test temporal datum
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testTemporalDatum() throws IOException {

		String text = "TIMEDATUM[\"Gregorian calendar\",CALENDAR[\"proleptic Gregorian\"],"
				+ "TIMEORIGIN[0000-01-01]]";
		CRSReader reader = new CRSReader(text);
		TemporalDatum temporalDatum = reader.readTemporalDatum();
		assertEquals("Gregorian calendar", temporalDatum.getName());
		assertEquals("proleptic Gregorian", temporalDatum.getCalendar());
		assertEquals("0000-01-01", temporalDatum.getOrigin());
		assertTrue(temporalDatum.hasOriginDateTime());
		assertEquals("0000-01-01",
				temporalDatum.getOriginDateTime().toString());
		reader.close();
		text = text.replaceAll("TIMEDATUM", "TDATUM");
		assertEquals(text, temporalDatum.toString());

		text = "TDATUM[\"Gregorian calendar\",TIMEORIGIN[\"0001 January 1st\"]]";
		reader = new CRSReader(text);
		temporalDatum = reader.readTemporalDatum();
		assertEquals("Gregorian calendar", temporalDatum.getName());
		assertFalse(temporalDatum.hasCalendar());
		assertEquals("0001 January 1st", temporalDatum.getOrigin());
		assertFalse(temporalDatum.hasOriginDateTime());
		reader.close();
		assertEquals(text, temporalDatum.toString());

		text = "TDATUM[\"Gregorian calendar\"]";
		reader = new CRSReader(text);
		temporalDatum = reader.readTemporalDatum();
		assertFalse(temporalDatum.hasCalendar());
		assertFalse(temporalDatum.hasOrigin());
		assertFalse(temporalDatum.hasOriginDateTime());
		reader.close();
		assertEquals(text, temporalDatum.toString());

	}

	/**
	 * Test temporal coordinate reference system
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testTemporalCoordinateReferenceSystem() throws IOException {

		String text = "TIMECRS[\"DateTime\","
				+ "TDATUM[\"Gregorian Calendar\"],"
				+ "CS[TemporalDateTime,1],AXIS[\"Time (T)\",future]]";

		CoordinateReferenceSystem crs = CRSReader.read(text, true);
		TemporalCoordinateReferenceSystem temporalCrs = CRSReader
				.readTemporal(text);
		assertEquals(crs, temporalCrs);
		assertEquals(CoordinateReferenceSystemType.TEMPORAL,
				temporalCrs.getType());
		assertEquals("DateTime", temporalCrs.getName());
		assertEquals("Gregorian Calendar",
				temporalCrs.getTemporalDatum().getName());
		assertEquals(CoordinateSystemType.TEMPORAL_DATE_TIME,
				temporalCrs.getCoordinateSystem().getType());
		assertEquals(1, temporalCrs.getCoordinateSystem().getDimension());
		assertEquals("Time",
				temporalCrs.getCoordinateSystem().getAxes().get(0).getName());
		assertEquals("T", temporalCrs.getCoordinateSystem().getAxes().get(0)
				.getAbbreviation());
		assertEquals(AxisDirectionType.FUTURE, temporalCrs.getCoordinateSystem()
				.getAxes().get(0).getDirection());
		text = text.replaceAll("TemporalDateTime", "temporalDateTime");
		assertEquals(text, temporalCrs.toString());
		assertEquals(text, CRSWriter.write(temporalCrs));
		assertEquals(WKTUtils.pretty(text), CRSWriter.writePretty(temporalCrs));

		text = "TIMECRS[\"GPS milliseconds\","
				+ "TDATUM[\"GPS time origin\",TIMEORIGIN[1980-01-01T00:00:00.0Z]],"
				+ "CS[TemporalCount,1],AXIS[\"(T)\",future,TIMEUNIT[\"millisecond (ms)\",0.001]]]";

		crs = CRSReader.read(text, true);
		temporalCrs = CRSReader.readTemporal(text);
		assertEquals(crs, temporalCrs);
		assertEquals(CoordinateReferenceSystemType.TEMPORAL,
				temporalCrs.getType());
		assertEquals("GPS milliseconds", temporalCrs.getName());
		assertEquals("GPS time origin",
				temporalCrs.getTemporalDatum().getName());
		assertEquals("1980-01-01T00:00:00.0Z",
				temporalCrs.getTemporalDatum().getOrigin());
		assertTrue(temporalCrs.getTemporalDatum().hasOriginDateTime());
		assertEquals("1980-01-01T00:00:00.0Z",
				temporalCrs.getTemporalDatum().getOriginDateTime().toString());
		assertEquals(CoordinateSystemType.TEMPORAL_COUNT,
				temporalCrs.getCoordinateSystem().getType());
		assertEquals(1, temporalCrs.getCoordinateSystem().getDimension());
		assertEquals("T", temporalCrs.getCoordinateSystem().getAxes().get(0)
				.getAbbreviation());
		assertEquals(AxisDirectionType.FUTURE, temporalCrs.getCoordinateSystem()
				.getAxes().get(0).getDirection());
		assertEquals(UnitType.TIMEUNIT, temporalCrs.getCoordinateSystem()
				.getAxes().get(0).getUnit().getType());
		assertEquals("millisecond (ms)", temporalCrs.getCoordinateSystem()
				.getAxes().get(0).getUnit().getName());
		assertEquals(0.001, temporalCrs.getCoordinateSystem().getAxes().get(0)
				.getUnit().getConversionFactor(), 0);
		text = text.replaceAll("TemporalCount", "temporalCount");
		assertEquals(text, temporalCrs.toString());
		assertEquals(text, CRSWriter.write(temporalCrs));
		assertEquals(WKTUtils.pretty(text), CRSWriter.writePretty(temporalCrs));

		text = "TIMECRS[\"Calendar hours from 1979-12-29\","
				+ "TDATUM[\"29 December 1979\",TIMEORIGIN[1979-12-29T00Z]],"
				+ "CS[TemporalCount,1],AXIS[\"Time\",future,TIMEUNIT[\"hour\"]]]";

		crs = CRSReader.read(text, true);
		temporalCrs = CRSReader.readTemporal(text);
		assertEquals(crs, temporalCrs);
		assertEquals(CoordinateReferenceSystemType.TEMPORAL,
				temporalCrs.getType());
		assertEquals("Calendar hours from 1979-12-29", temporalCrs.getName());
		assertEquals("29 December 1979",
				temporalCrs.getTemporalDatum().getName());
		assertEquals("1979-12-29T00Z",
				temporalCrs.getTemporalDatum().getOrigin());
		assertTrue(temporalCrs.getTemporalDatum().hasOriginDateTime());
		assertEquals("1979-12-29T00Z",
				temporalCrs.getTemporalDatum().getOriginDateTime().toString());
		assertEquals(CoordinateSystemType.TEMPORAL_COUNT,
				temporalCrs.getCoordinateSystem().getType());
		assertEquals(1, temporalCrs.getCoordinateSystem().getDimension());
		assertEquals("Time",
				temporalCrs.getCoordinateSystem().getAxes().get(0).getName());
		assertEquals(AxisDirectionType.FUTURE, temporalCrs.getCoordinateSystem()
				.getAxes().get(0).getDirection());
		assertEquals(UnitType.TIMEUNIT, temporalCrs.getCoordinateSystem()
				.getAxes().get(0).getUnit().getType());
		assertEquals("hour", temporalCrs.getCoordinateSystem().getAxes().get(0)
				.getUnit().getName());
		text = text.replaceAll("TemporalCount", "temporalCount");
		assertEquals(text, temporalCrs.toString());
		assertEquals(text, CRSWriter.write(temporalCrs));
		assertEquals(WKTUtils.pretty(text), CRSWriter.writePretty(temporalCrs));

		text = "TIMECRS[\"Decimal Years CE\","
				+ "TDATUM[\"Common Era\",TIMEORIGIN[0000]],"
				+ "CS[TemporalMeasure,1],AXIS[\"Decimal years (a)\",future,TIMEUNIT[\"year\"]]]";

		crs = CRSReader.read(text, true);
		temporalCrs = CRSReader.readTemporal(text);
		assertEquals(crs, temporalCrs);
		assertEquals(CoordinateReferenceSystemType.TEMPORAL,
				temporalCrs.getType());
		assertEquals("Decimal Years CE", temporalCrs.getName());
		assertEquals("Common Era", temporalCrs.getTemporalDatum().getName());
		assertEquals("0000", temporalCrs.getTemporalDatum().getOrigin());
		assertTrue(temporalCrs.getTemporalDatum().hasOriginDateTime());
		assertEquals("0000",
				temporalCrs.getTemporalDatum().getOriginDateTime().toString());
		assertEquals(CoordinateSystemType.TEMPORAL_MEASURE,
				temporalCrs.getCoordinateSystem().getType());
		assertEquals(1, temporalCrs.getCoordinateSystem().getDimension());
		assertEquals("Decimal years",
				temporalCrs.getCoordinateSystem().getAxes().get(0).getName());
		assertEquals("a", temporalCrs.getCoordinateSystem().getAxes().get(0)
				.getAbbreviation());
		assertEquals(AxisDirectionType.FUTURE, temporalCrs.getCoordinateSystem()
				.getAxes().get(0).getDirection());
		assertEquals(UnitType.TIMEUNIT, temporalCrs.getCoordinateSystem()
				.getAxes().get(0).getUnit().getType());
		assertEquals("year", temporalCrs.getCoordinateSystem().getAxes().get(0)
				.getUnit().getName());
		text = text.replaceAll("TemporalMeasure", "temporalMeasure");
		assertEquals(text, temporalCrs.toString());
		assertEquals(text, CRSWriter.write(temporalCrs));
		assertEquals(WKTUtils.pretty(text), CRSWriter.writePretty(temporalCrs));

		text = "TIMECRS[\"Unix time\","
				+ "TDATUM[\"Unix epoch\",TIMEORIGIN[1970-01-01T00:00:00Z]],"
				+ "CS[TemporalCount,1],AXIS[\"Time\",future,TIMEUNIT[\"second\"]]]";

		crs = CRSReader.read(text, true);
		temporalCrs = CRSReader.readTemporal(text);
		assertEquals(crs, temporalCrs);
		assertEquals(CoordinateReferenceSystemType.TEMPORAL,
				temporalCrs.getType());
		assertEquals("Unix time", temporalCrs.getName());
		assertEquals("Unix epoch", temporalCrs.getTemporalDatum().getName());
		assertEquals("1970-01-01T00:00:00Z",
				temporalCrs.getTemporalDatum().getOrigin());
		assertTrue(temporalCrs.getTemporalDatum().hasOriginDateTime());
		assertEquals("1970-01-01T00:00:00Z",
				temporalCrs.getTemporalDatum().getOriginDateTime().toString());
		assertEquals(CoordinateSystemType.TEMPORAL_COUNT,
				temporalCrs.getCoordinateSystem().getType());
		assertEquals(1, temporalCrs.getCoordinateSystem().getDimension());
		assertEquals("Time",
				temporalCrs.getCoordinateSystem().getAxes().get(0).getName());
		assertEquals(AxisDirectionType.FUTURE, temporalCrs.getCoordinateSystem()
				.getAxes().get(0).getDirection());
		assertEquals(UnitType.TIMEUNIT, temporalCrs.getCoordinateSystem()
				.getAxes().get(0).getUnit().getType());
		assertEquals("second", temporalCrs.getCoordinateSystem().getAxes()
				.get(0).getUnit().getName());
		text = text.replaceAll("TemporalCount", "temporalCount");
		assertEquals(text, temporalCrs.toString());
		assertEquals(text, CRSWriter.write(temporalCrs));
		assertEquals(WKTUtils.pretty(text), CRSWriter.writePretty(temporalCrs));

	}

	/**
	 * Test Backward Compatibility map projection
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testMapProjectionCompat() throws IOException {

		String text = "PROJECTION[\"Transverse Mercator\"],"
				+ "PARAMETER[\"Latitude of origin\",0],"
				+ "PARAMETER[\"Central meridian\",-123],"
				+ "PARAMETER[\"Scale factor\",0.9996],"
				+ "PARAMETER[\"False easting\",500000],"
				+ "PARAMETER[\"False northing\",0]";
		CRSReader reader = new CRSReader(text);
		MapProjection mapProjection = reader.readMapProjectionCompat();
		assertEquals("Transverse Mercator", mapProjection.getName());
		assertEquals("Transverse Mercator", mapProjection.getMethodName());
		assertEquals("Latitude of origin",
				mapProjection.getParameters().get(0).getName());
		assertEquals(0, mapProjection.getParameters().get(0).getValue(), 0);
		assertEquals("Central meridian",
				mapProjection.getParameters().get(1).getName());
		assertEquals(-123, mapProjection.getParameters().get(1).getValue(), 0);
		assertEquals("Scale factor",
				mapProjection.getParameters().get(2).getName());
		assertEquals(0.9996, mapProjection.getParameters().get(2).getValue(),
				0);
		assertEquals("False easting",
				mapProjection.getParameters().get(3).getName());
		assertEquals(500000, mapProjection.getParameters().get(3).getValue(),
				0);
		assertEquals("False northing",
				mapProjection.getParameters().get(4).getName());
		assertEquals(0, mapProjection.getParameters().get(4).getValue(), 0);
		reader.close();

		String expectedText = "CONVERSION[\"Transverse Mercator\",METHOD[\"Transverse Mercator\"],"
				+ "PARAMETER[\"Latitude of origin\",0.0],"
				+ "PARAMETER[\"Central meridian\",-123.0],"
				+ "PARAMETER[\"Scale factor\",0.9996],"
				+ "PARAMETER[\"False easting\",500000.0],"
				+ "PARAMETER[\"False northing\",0.0]]";

		assertEquals(expectedText, mapProjection.toString());

		text = "PROJECTION[\"UTM zone 10N\"],"
				+ "PARAMETER[\"Latitude of natural origin\",0],"
				+ "PARAMETER[\"Longitude of natural origin\",-123],"
				+ "PARAMETER[\"Scale factor at natural origin\",0.9996],"
				+ "PARAMETER[\"FE\",500000],PARAMETER[\"FN\",0]";
		reader = new CRSReader(text);
		mapProjection = reader.readMapProjectionCompat();
		assertEquals("UTM zone 10N", mapProjection.getName());
		assertEquals("UTM zone 10N", mapProjection.getMethodName());
		assertEquals("Latitude of natural origin",
				mapProjection.getParameters().get(0).getName());
		assertEquals(0, mapProjection.getParameters().get(0).getValue(), 0);
		assertEquals("Longitude of natural origin",
				mapProjection.getParameters().get(1).getName());
		assertEquals(-123, mapProjection.getParameters().get(1).getValue(), 0);
		assertEquals("Scale factor at natural origin",
				mapProjection.getParameters().get(2).getName());
		assertEquals(0.9996, mapProjection.getParameters().get(2).getValue(),
				0);
		assertEquals("FE", mapProjection.getParameters().get(3).getName());
		assertEquals(500000, mapProjection.getParameters().get(3).getValue(),
				0);
		assertEquals("FN", mapProjection.getParameters().get(4).getName());
		assertEquals(0, mapProjection.getParameters().get(4).getValue(), 0);
		reader.close();

		expectedText = "CONVERSION[\"UTM zone 10N\",METHOD[\"UTM zone 10N\"],"
				+ "PARAMETER[\"Latitude of natural origin\",0.0],"
				+ "PARAMETER[\"Longitude of natural origin\",-123.0],"
				+ "PARAMETER[\"Scale factor at natural origin\",0.9996],"
				+ "PARAMETER[\"FE\",500000.0],PARAMETER[\"FN\",0.0]]";

		assertEquals(expectedText, mapProjection.toString());

	}

	/**
	 * Test Backward Compatibility unit
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testUnitCompat() throws IOException {

		String text = "UNIT[\"German legal metre\",1.0000135965]";
		CRSReader reader = new CRSReader(text);
		Unit unit = reader.readUnit();
		assertEquals(UnitType.UNIT, unit.getType());
		assertEquals("German legal metre", unit.getName());
		assertEquals(1.0000135965, unit.getConversionFactor(), 0);
		reader.close();
		assertEquals(text, unit.toString());

	}

	/**
	 * Test Backward Compatibility axis
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testAxisCompat() throws IOException {

		String text = "AXIS[\"northing\",north]";
		CRSReader reader = new CRSReader(text);
		Axis axis = reader.readAxis();
		assertEquals("northing", axis.getName());
		assertEquals(AxisDirectionType.NORTH, axis.getDirection());
		reader.close();
		assertEquals(text, axis.toString());

		text = "AXIS[\"easting\",east]";
		reader = new CRSReader(text);
		axis = reader.readAxis();
		assertEquals("easting", axis.getName());
		assertEquals(AxisDirectionType.EAST, axis.getDirection());
		reader.close();
		assertEquals(text, axis.toString());

	}

	/**
	 * Test Backward Compatibility of Geographic CRS
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testGeographicCompat() throws IOException {

		String text = "GEOGCS[\"NAD83\","
				+ "DATUM[\"North American Datum 1983\","
				+ "ELLIPSOID[\"GRS 1980\",6378137.0,298.257222101]],"
				+ "PRIMEM[\"Greenwich\",0],"
				+ "UNIT[\"degree\",0.0174532925199433]]";

		CoordinateReferenceSystem crs = CRSReader.read(text, true);
		GeodeticCoordinateReferenceSystem geodeticOrGeographicCrs = CRSReader
				.readGeodeticOrGeographicCompat(text);
		assertEquals(crs, geodeticOrGeographicCrs);
		GeodeticCoordinateReferenceSystem geographicCrs = CRSReader
				.readGeographicCompat(text);
		assertEquals(crs, geographicCrs);
		assertEquals(CoordinateReferenceSystemType.GEOGRAPHIC,
				geographicCrs.getType());
		assertEquals("NAD83", geographicCrs.getName());
		assertEquals("North American Datum 1983",
				geographicCrs.getGeodeticReferenceFrame().getName());
		assertEquals("GRS 1980", geographicCrs.getGeodeticReferenceFrame()
				.getEllipsoid().getName());
		assertEquals(6378137.0, geographicCrs.getGeodeticReferenceFrame()
				.getEllipsoid().getSemiMajorAxis(), 0);
		assertEquals(298.257222101, geographicCrs.getGeodeticReferenceFrame()
				.getEllipsoid().getInverseFlattening(), 0);
		assertEquals("Greenwich", geographicCrs.getGeodeticReferenceFrame()
				.getPrimeMeridian().getName());
		assertEquals(0.0, geographicCrs.getGeodeticReferenceFrame()
				.getPrimeMeridian().getIrmLongitude(), 0);
		assertEquals(CoordinateSystemType.ELLIPSOIDAL,
				geographicCrs.getCoordinateSystem().getType());
		assertEquals(2, geographicCrs.getCoordinateSystem().getDimension());
		assertEquals("Lon",
				geographicCrs.getCoordinateSystem().getAxes().get(0).getName());
		assertEquals(AxisDirectionType.EAST, geographicCrs.getCoordinateSystem()
				.getAxes().get(0).getDirection());
		assertEquals("Lat",
				geographicCrs.getCoordinateSystem().getAxes().get(1).getName());
		assertEquals(AxisDirectionType.NORTH, geographicCrs
				.getCoordinateSystem().getAxes().get(1).getDirection());
		assertEquals("degree",
				geographicCrs.getCoordinateSystem().getUnit().getName());
		assertEquals(0.0174532925199433, geographicCrs.getCoordinateSystem()
				.getUnit().getConversionFactor(), 0);

		String expectedText = "GEOGCRS[\"NAD83\","
				+ "DATUM[\"North American Datum 1983\","
				+ "ELLIPSOID[\"GRS 1980\",6378137.0,298.257222101]],"
				+ "PRIMEM[\"Greenwich\",0.0],CS[ellipsoidal,2],"
				+ "AXIS[\"Lon\",east],AXIS[\"Lat\",north],UNIT[\"degree\",0.0174532925199433]]";

		assertEquals(expectedText, crs.toString());
		assertEquals(expectedText, CRSWriter.write(crs));
		assertEquals(WKTUtils.pretty(expectedText), CRSWriter.writePretty(crs));

		text = "GEOGCS[\"NAD83\",DATUM[\"North American Datum 1983\","
				+ "SPHEROID[\"GRS 1980\",6378137.0,298.257222101]],"
				+ "PRIMEM[\"Greenwich\",0],"
				+ "AXIS[\"latitude\",NORTH],AXIS[\"longitude\",EAST],"
				+ "UNIT[\"degree\",0.0174532925199433]]";

		crs = CRSReader.read(text, true);
		geodeticOrGeographicCrs = CRSReader
				.readGeodeticOrGeographicCompat(text);
		assertEquals(crs, geodeticOrGeographicCrs);
		geographicCrs = CRSReader.readGeographicCompat(text);
		assertEquals(crs, geographicCrs);
		assertEquals(CoordinateReferenceSystemType.GEOGRAPHIC,
				geographicCrs.getType());
		assertEquals("NAD83", geographicCrs.getName());
		assertEquals("North American Datum 1983",
				geographicCrs.getGeodeticReferenceFrame().getName());
		assertEquals("GRS 1980", geographicCrs.getGeodeticReferenceFrame()
				.getEllipsoid().getName());
		assertEquals(6378137.0, geographicCrs.getGeodeticReferenceFrame()
				.getEllipsoid().getSemiMajorAxis(), 0);
		assertEquals(298.257222101, geographicCrs.getGeodeticReferenceFrame()
				.getEllipsoid().getInverseFlattening(), 0);
		assertEquals("Greenwich", geographicCrs.getGeodeticReferenceFrame()
				.getPrimeMeridian().getName());
		assertEquals(0.0, geographicCrs.getGeodeticReferenceFrame()
				.getPrimeMeridian().getIrmLongitude(), 0);
		assertEquals(CoordinateSystemType.ELLIPSOIDAL,
				geographicCrs.getCoordinateSystem().getType());
		assertEquals(2, geographicCrs.getCoordinateSystem().getDimension());
		assertEquals("latitude",
				geographicCrs.getCoordinateSystem().getAxes().get(0).getName());
		assertEquals(AxisDirectionType.NORTH, geographicCrs
				.getCoordinateSystem().getAxes().get(0).getDirection());
		assertEquals("longitude",
				geographicCrs.getCoordinateSystem().getAxes().get(1).getName());
		assertEquals(AxisDirectionType.EAST, geographicCrs.getCoordinateSystem()
				.getAxes().get(1).getDirection());
		assertEquals("degree",
				geographicCrs.getCoordinateSystem().getUnit().getName());
		assertEquals(0.0174532925199433, geographicCrs.getCoordinateSystem()
				.getUnit().getConversionFactor(), 0);

		expectedText = "GEOGCRS[\"NAD83\","
				+ "DATUM[\"North American Datum 1983\","
				+ "ELLIPSOID[\"GRS 1980\",6378137.0,298.257222101]],"
				+ "PRIMEM[\"Greenwich\",0.0],CS[ellipsoidal,2],"
				+ "AXIS[\"latitude\",north],AXIS[\"longitude\",east],"
				+ "UNIT[\"degree\",0.0174532925199433]]";

		assertEquals(expectedText, crs.toString());
		assertEquals(expectedText, CRSWriter.write(crs));
		assertEquals(WKTUtils.pretty(expectedText), CRSWriter.writePretty(crs));

	}

	/**
	 * Test Backward Compatibility of Projected CRS
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testProjectedCompat() throws IOException {

		String text = "PROJCS[\"NAD83 / UTM zone 10N\",GEOGCS[\"NAD83\","
				+ "DATUM[\"North American Datum 1983\","
				+ "SPHEROID[\"GRS 1980\",6378137.0,298.257222101]],"
				+ "PRIMEM[\"Greenwich\",0]],"
				+ "PROJECTION[\"Transverse Mercator\"],"
				+ "PARAMETER[\"Latitude of origin\",0.0],"
				+ "PARAMETER[\"Longitude of origin\",-123],"
				+ "PARAMETER[\"Scale factor\",0.9996],"
				+ "PARAMETER[\"False easting\",500000],"
				+ "PARAMETER[\"False northing\",0],UNIT[\"metre\",1.0]]";

		CoordinateReferenceSystem crs = CRSReader.read(text, true);
		ProjectedCoordinateReferenceSystem projectedCrs = CRSReader
				.readProjectedCompat(text);
		assertEquals(crs, projectedCrs);
		assertEquals(CoordinateReferenceSystemType.PROJECTED,
				projectedCrs.getType());
		assertEquals("NAD83 / UTM zone 10N", projectedCrs.getName());
		assertEquals(CoordinateReferenceSystemType.GEOGRAPHIC,
				projectedCrs.getBaseType());
		assertEquals("NAD83", projectedCrs.getBaseName());
		assertEquals("North American Datum 1983",
				projectedCrs.getGeodeticReferenceFrame().getName());
		assertEquals("GRS 1980", projectedCrs.getGeodeticReferenceFrame()
				.getEllipsoid().getName());
		assertEquals(6378137.0, projectedCrs.getGeodeticReferenceFrame()
				.getEllipsoid().getSemiMajorAxis(), 0);
		assertEquals(298.257222101, projectedCrs.getGeodeticReferenceFrame()
				.getEllipsoid().getInverseFlattening(), 0);
		assertEquals("Greenwich", projectedCrs.getGeodeticReferenceFrame()
				.getPrimeMeridian().getName());
		assertEquals(0, projectedCrs.getGeodeticReferenceFrame()
				.getPrimeMeridian().getIrmLongitude(), 0);
		assertEquals("Transverse Mercator",
				projectedCrs.getMapProjection().getName());
		assertEquals("Transverse Mercator",
				projectedCrs.getMapProjection().getMethodName());
		assertEquals("Latitude of origin", projectedCrs.getMapProjection()
				.getParameters().get(0).getName());
		assertEquals(0.0, projectedCrs.getMapProjection().getParameters().get(0)
				.getValue(), 0);
		assertEquals("Longitude of origin", projectedCrs.getMapProjection()
				.getParameters().get(1).getName());
		assertEquals(-123, projectedCrs.getMapProjection().getParameters()
				.get(1).getValue(), 0);
		assertEquals("Scale factor", projectedCrs.getMapProjection()
				.getParameters().get(2).getName());
		assertEquals(0.9996, projectedCrs.getMapProjection().getParameters()
				.get(2).getValue(), 0);
		assertEquals("False easting", projectedCrs.getMapProjection()
				.getParameters().get(3).getName());
		assertEquals(500000, projectedCrs.getMapProjection().getParameters()
				.get(3).getValue(), 0);
		assertEquals("False northing", projectedCrs.getMapProjection()
				.getParameters().get(4).getName());
		assertEquals(0, projectedCrs.getMapProjection().getParameters().get(4)
				.getValue(), 0);
		assertEquals(UnitType.UNIT,
				projectedCrs.getCoordinateSystem().getUnit().getType());
		assertEquals("metre",
				projectedCrs.getCoordinateSystem().getUnit().getName());
		assertEquals(1.0, projectedCrs.getCoordinateSystem().getUnit()
				.getConversionFactor(), 0);

		String expectedText = "PROJCRS[\"NAD83 / UTM zone 10N\",BASEGEOGCRS[\"NAD83\","
				+ "DATUM[\"North American Datum 1983\","
				+ "ELLIPSOID[\"GRS 1980\",6378137.0,298.257222101]],"
				+ "PRIMEM[\"Greenwich\",0.0]],"
				+ "CONVERSION[\"Transverse Mercator\",METHOD[\"Transverse Mercator\"],"
				+ "PARAMETER[\"Latitude of origin\",0.0],"
				+ "PARAMETER[\"Longitude of origin\",-123.0],"
				+ "PARAMETER[\"Scale factor\",0.9996],"
				+ "PARAMETER[\"False easting\",500000.0],"
				+ "PARAMETER[\"False northing\",0.0]],"
				+ "CS[ellipsoidal,2],AXIS[\"X\",east],AXIS[\"Y\",north],"
				+ "UNIT[\"metre\",1.0]]";

		assertEquals(expectedText, projectedCrs.toString());
		assertEquals(expectedText, CRSWriter.write(projectedCrs));
		assertEquals(WKTUtils.pretty(expectedText),
				CRSWriter.writePretty(projectedCrs));

		text = "PROJCS[\"NAD83 / UTM zone 10N\",GEOGCS[\"NAD83\","
				+ "DATUM[\"North American Datum 1983\","
				+ "ELLIPSOID[\"GRS 1980\",6378137.0,298.257222101]],"
				+ "PRIMEM[\"Greenwich\",0],"
				+ "AXIS[\"latitude\",NORTH],AXIS[\"longitude\",EAST]],"
				+ "PROJECTION[\"UTM zone 10N\"],"
				+ "PARAMETER[\"Latitude of origin\",0.0],"
				+ "PARAMETER[\"Longitude of origin\",-123],"
				+ "PARAMETER[\"Scale factor at natural origin\",0.9996],"
				+ "PARAMETER[\"FE\",500000],PARAMETER[\"FN\",0],"
				+ "AXIS[\"easting\",EAST],AXIS[\"northing\",NORTH],"
				+ "UNIT[\"metre\",1.0]]";

		crs = CRSReader.read(text, true);
		projectedCrs = CRSReader.readProjectedCompat(text);
		assertEquals(crs, projectedCrs);
		assertEquals(CoordinateReferenceSystemType.PROJECTED,
				projectedCrs.getType());
		assertEquals("NAD83 / UTM zone 10N", projectedCrs.getName());
		assertEquals(CoordinateReferenceSystemType.GEOGRAPHIC,
				projectedCrs.getBaseType());
		assertEquals("NAD83", projectedCrs.getBaseName());
		assertEquals("North American Datum 1983",
				projectedCrs.getGeodeticReferenceFrame().getName());
		assertEquals("GRS 1980", projectedCrs.getGeodeticReferenceFrame()
				.getEllipsoid().getName());
		assertEquals(6378137.0, projectedCrs.getGeodeticReferenceFrame()
				.getEllipsoid().getSemiMajorAxis(), 0);
		assertEquals(298.257222101, projectedCrs.getGeodeticReferenceFrame()
				.getEllipsoid().getInverseFlattening(), 0);
		assertEquals("Greenwich", projectedCrs.getGeodeticReferenceFrame()
				.getPrimeMeridian().getName());
		assertEquals(0, projectedCrs.getGeodeticReferenceFrame()
				.getPrimeMeridian().getIrmLongitude(), 0);
		assertEquals("UTM zone 10N", projectedCrs.getMapProjection().getName());
		assertEquals("UTM zone 10N",
				projectedCrs.getMapProjection().getMethodName());
		assertEquals("Latitude of origin", projectedCrs.getMapProjection()
				.getParameters().get(0).getName());
		assertEquals(0.0, projectedCrs.getMapProjection().getParameters().get(0)
				.getValue(), 0);
		assertEquals("Longitude of origin", projectedCrs.getMapProjection()
				.getParameters().get(1).getName());
		assertEquals(-123, projectedCrs.getMapProjection().getParameters()
				.get(1).getValue(), 0);
		assertEquals("Scale factor at natural origin", projectedCrs
				.getMapProjection().getParameters().get(2).getName());
		assertEquals(0.9996, projectedCrs.getMapProjection().getParameters()
				.get(2).getValue(), 0);
		assertEquals("FE", projectedCrs.getMapProjection().getParameters()
				.get(3).getName());
		assertEquals(500000, projectedCrs.getMapProjection().getParameters()
				.get(3).getValue(), 0);
		assertEquals("FN", projectedCrs.getMapProjection().getParameters()
				.get(4).getName());
		assertEquals(0, projectedCrs.getMapProjection().getParameters().get(4)
				.getValue(), 0);
		assertEquals(UnitType.UNIT,
				projectedCrs.getCoordinateSystem().getUnit().getType());
		assertEquals("metre",
				projectedCrs.getCoordinateSystem().getUnit().getName());
		assertEquals(1.0, projectedCrs.getCoordinateSystem().getUnit()
				.getConversionFactor(), 0);

		expectedText = "PROJCRS[\"NAD83 / UTM zone 10N\",BASEGEOGCRS[\"NAD83\","
				+ "DATUM[\"North American Datum 1983\","
				+ "ELLIPSOID[\"GRS 1980\",6378137.0,298.257222101]],"
				+ "PRIMEM[\"Greenwich\",0.0]],"
				+ "CONVERSION[\"UTM zone 10N\",METHOD[\"UTM zone 10N\"],"
				+ "PARAMETER[\"Latitude of origin\",0.0],"
				+ "PARAMETER[\"Longitude of origin\",-123.0],"
				+ "PARAMETER[\"Scale factor at natural origin\",0.9996],"
				+ "PARAMETER[\"FE\",500000.0],PARAMETER[\"FN\",0.0]],"
				+ "CS[ellipsoidal,2],"
				+ "AXIS[\"easting\",east],AXIS[\"northing\",north],"
				+ "UNIT[\"metre\",1.0]]";

		assertEquals(expectedText, projectedCrs.toString());
		assertEquals(expectedText, CRSWriter.write(projectedCrs));
		assertEquals(WKTUtils.pretty(expectedText),
				CRSWriter.writePretty(projectedCrs));

	}

}