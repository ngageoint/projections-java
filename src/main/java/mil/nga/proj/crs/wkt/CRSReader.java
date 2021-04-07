package mil.nga.proj.crs.wkt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mil.nga.proj.PrimeMeridian;
import mil.nga.proj.ProjectionException;
import mil.nga.proj.crs.Axis;
import mil.nga.proj.crs.AxisDirectionType;
import mil.nga.proj.crs.CoordinateReferenceSystem;
import mil.nga.proj.crs.CoordinateSystem;
import mil.nga.proj.crs.CoordinateSystemType;
import mil.nga.proj.crs.Ellipsoid;
import mil.nga.proj.crs.GeodeticReferenceFrame;
import mil.nga.proj.crs.Identifier;
import mil.nga.proj.crs.ScopeExtentIdentifierRemark;
import mil.nga.proj.crs.Unit;
import mil.nga.proj.crs.UnitType;

/**
 * Well Known Text reader
 * 
 * @author osbornb
 */
public class CRSReader {

	/**
	 * Read a Coordinate Reference System from the well-known text
	 * 
	 * @param text
	 *            well-known text
	 * @return Coordinate Reference System
	 * @throws IOException
	 *             upon failure to read
	 */
	public static CoordinateReferenceSystem readCRS(String text)
			throws IOException {
		CoordinateReferenceSystem crs = null;
		CRSReader reader = new CRSReader(text);
		try {
			crs = reader.read();
		} finally {
			reader.close();
		}
		return crs;
	}

	/**
	 * Text Reader
	 */
	private TextReader reader;

	/**
	 * Constructor
	 * 
	 * @param text
	 *            well-known text
	 */
	public CRSReader(String text) {
		this(new TextReader(text));
	}

	/**
	 * Constructor
	 * 
	 * @param reader
	 *            text reader
	 */
	public CRSReader(TextReader reader) {
		this.reader = reader;
	}

	/**
	 * Get the text reader
	 * 
	 * @return text reader
	 */
	public TextReader getTextReader() {
		return reader;
	}

	/**
	 * Close the text reader
	 */
	public void close() {
		reader.close();
	}

	/**
	 * Read a Coordinate Reference System from the well-known text
	 * 
	 * @return Coordinate Reference System
	 * @throws IOException
	 *             upon failure to read
	 */
	public CoordinateReferenceSystem read() throws IOException {

		CoordinateReferenceSystem crs = null;

		CoordinateReferenceSystemKeyword keyword = peekKeyword();
		switch (keyword) {
		case GEODCRS:
		case GEOGCRS:
			crs = readGeodeticOrGeographic();
			break;
		default:
			throw new ProjectionException(
					"Unsupported WKT CRS keyword: " + keyword);
		}

		return crs;
	}

	/**
	 * Read a WKT CRS keyword
	 * 
	 * @return keyword
	 * @throws IOException
	 *             upon failure to read
	 */
	public CoordinateReferenceSystemKeyword readKeyword() throws IOException {
		return CoordinateReferenceSystemKeyword
				.getRequiredType(reader.readToken());
	}

	/**
	 * Read WKT CRS keywords
	 * 
	 * @return keywords
	 * @throws IOException
	 *             upon failure to read
	 */
	public Set<CoordinateReferenceSystemKeyword> readKeywords()
			throws IOException {
		return CoordinateReferenceSystemKeyword
				.getRequiredTypes(reader.readToken());
	}

	/**
	 * Peek a WKT CRS keyword
	 * 
	 * @return keyword
	 * @throws IOException
	 *             upon failure to read
	 */
	public CoordinateReferenceSystemKeyword peekKeyword() throws IOException {
		return CoordinateReferenceSystemKeyword
				.getRequiredType(reader.peekToken());
	}

	/**
	 * Peek WKT CRS keywords
	 * 
	 * @return keywords
	 * @throws IOException
	 *             upon failure to read
	 */
	public Set<CoordinateReferenceSystemKeyword> peekKeywords()
			throws IOException {
		return CoordinateReferenceSystemKeyword
				.getRequiredTypes(reader.peekToken());
	}

	/**
	 * Peek an optional WKT CRS keyword
	 * 
	 * @return keyword
	 * @throws IOException
	 *             upon failure to read
	 */
	public CoordinateReferenceSystemKeyword peekOptionalKeyword()
			throws IOException {
		return CoordinateReferenceSystemKeyword.getType(reader.peekToken());
	}

	/**
	 * Peek an optional WKT CRS keywords
	 * 
	 * @return keywords
	 * @throws IOException
	 *             upon failure to read
	 */
	public Set<CoordinateReferenceSystemKeyword> peekOptionalKeywords()
			throws IOException {
		return CoordinateReferenceSystemKeyword.getTypes(reader.peekToken());
	}

	/**
	 * Peek an optional WKT CRS keyword
	 * 
	 * @param num
	 *            number of tokens out to peek at
	 * @return keyword
	 * @throws IOException
	 *             upon failure to read
	 */
	public CoordinateReferenceSystemKeyword peekOptionalKeyword(int num)
			throws IOException {
		return CoordinateReferenceSystemKeyword.getType(reader.peekToken(num));
	}

	/**
	 * Peek an optional WKT CRS keywords
	 * 
	 * @param num
	 *            number of tokens out to peek at
	 * @return keywords
	 * @throws IOException
	 *             upon failure to read
	 */
	public Set<CoordinateReferenceSystemKeyword> peekOptionalKeywords(int num)
			throws IOException {
		return CoordinateReferenceSystemKeyword.getTypes(reader.peekToken(num));
	}

	/**
	 * Read a left delimiter
	 * 
	 * @throws IOException
	 *             upon failure to read
	 */
	public void readLeftDelimiter() throws IOException {
		String token = reader.readExpectedToken();
		if (!token.equals("[") && !token.equals("(")) {
			throw new ProjectionException(
					"Invalid left delimiter token, expected '[' or '('. found: '"
							+ token + "'");
		}
	}

	/**
	 * Read a right delimiter
	 * 
	 * @throws IOException
	 *             upon failure to read
	 */
	public void readRightDelimiter() throws IOException {
		String token = reader.readExpectedToken();
		if (!token.equals("]") && !token.equals(")")) {
			throw new ProjectionException(
					"Invalid right delimiter token, expected ']' or ')'. found: '"
							+ token + "'");
		}
	}

	/**
	 * Read a WKT Separator (comma)
	 * 
	 * @throws IOException
	 *             upon failure to read
	 */
	public void readSeparator() throws IOException {
		String token = reader.readExpectedToken();
		if (!token.equals(",")) {
			throw new ProjectionException(
					"Invalid separator token, expected ','. found: '" + token
							+ "'");
		}
	}

	/**
	 * Peek if the next token is a WKT Separator (comma)
	 * 
	 * @return true if next token is a separator
	 * @throws IOException
	 *             upon failure to read
	 */
	public boolean peekSeparator() throws IOException {
		return reader.peekExpectedToken().equals(",");
	}

	/**
	 * Read a keyword for the quoted text within delimiters
	 * 
	 * @param keyword
	 *            expected keyword
	 * @return text
	 * @throws IOException
	 *             upon failure to read
	 */
	public String readKeywordDelimitedQuotedText(
			CoordinateReferenceSystemKeyword keyword) throws IOException {

		validateKeyword(readKeyword(), keyword);

		readLeftDelimiter();

		String text = reader.readQuotedText();

		readRightDelimiter();

		return text;
	}

	/**
	 * Validate the keyword against the expected
	 * 
	 * @param keyword
	 *            keyword
	 * @param expected
	 *            expected keyword
	 */
	private void validateKeyword(CoordinateReferenceSystemKeyword keyword,
			CoordinateReferenceSystemKeyword expected) {
		if (keyword != expected) {
			throw new ProjectionException("Unexpected keyword. found: "
					+ keyword + ", expected: " + expected);
		}
	}

	/**
	 * Validate the keyword against the expected keywords
	 * 
	 * @param keyword
	 *            keyword
	 * @param expected
	 *            expected keywords
	 */
	private void validateKeyword(CoordinateReferenceSystemKeyword keyword,
			CoordinateReferenceSystemKeyword... expected) {
		Set<CoordinateReferenceSystemKeyword> expectedSet = new HashSet<>(
				Arrays.asList(expected));
		if (!expectedSet.contains(keyword)) {
			throw new ProjectionException("Unexpected keyword. found: "
					+ keyword + ", expected: " + expectedSet);
		}
	}

	/**
	 * Validate the keywords against the expected keyword
	 * 
	 * @param keywords
	 *            keywords
	 * @param expected
	 *            expected keyword
	 */
	private void validateKeywords(
			Set<CoordinateReferenceSystemKeyword> keywords,
			CoordinateReferenceSystemKeyword expected) {
		if (!keywords.contains(expected)) {
			throw new ProjectionException("Unexpected keyword. found: "
					+ keywords + ", expected: " + expected);
		}
	}

	/**
	 * Check if the keyword is next following an immediate next separator
	 * 
	 * @param keywords
	 *            keywords
	 * @return true if next
	 * @throws IOException
	 *             upon failure to read
	 */
	private boolean isKeywordNext(CoordinateReferenceSystemKeyword... keywords)
			throws IOException {
		boolean next = false;
		if (peekSeparator()) {
			Set<CoordinateReferenceSystemKeyword> nextKeywords = peekOptionalKeywords(
					2);
			if (nextKeywords != null && !nextKeywords.isEmpty()) {
				for (CoordinateReferenceSystemKeyword keyword : keywords) {
					next = nextKeywords.contains(keyword);
					if (next) {
						break;
					}
				}
			}
		}
		return next;
	}

	/**
	 * Check if the keyword is next following an immediate next separator
	 * 
	 * @param keyword
	 *            keyword
	 * @return true if next
	 * @throws IOException
	 *             upon failure to read
	 */
	private boolean isNonKeywordNext() throws IOException {
		boolean next = false;
		if (peekSeparator()) {
			Set<CoordinateReferenceSystemKeyword> nextKeywords = peekOptionalKeywords(
					2);
			next = nextKeywords == null || nextKeywords.isEmpty();
		}
		return next;
	}

	/**
	 * Is a unit next following an immediate next separator
	 * 
	 * @return true if next
	 * @throws IOException
	 *             upon failure to read
	 */
	private boolean isUnitNext() throws IOException {
		return isKeywordNext(CoordinateReferenceSystemKeyword.ANGLEUNIT,
				CoordinateReferenceSystemKeyword.LENGTHUNIT,
				CoordinateReferenceSystemKeyword.PARAMETRICUNIT,
				CoordinateReferenceSystemKeyword.SCALEUNIT,
				CoordinateReferenceSystemKeyword.TIMEUNIT);
	}

	/**
	 * Is a spatial unit next following an immediate next separator
	 * 
	 * @return true if next
	 * @throws IOException
	 *             upon failure to read
	 */
	private boolean isSpatialUnitNext() throws IOException {
		return isKeywordNext(CoordinateReferenceSystemKeyword.ANGLEUNIT,
				CoordinateReferenceSystemKeyword.LENGTHUNIT,
				CoordinateReferenceSystemKeyword.PARAMETRICUNIT,
				CoordinateReferenceSystemKeyword.SCALEUNIT);
	}

	/**
	 * Is a time unit next following an immediate next separator
	 * 
	 * @return true if next
	 * @throws IOException
	 *             upon failure to read
	 */
	private boolean isTimeUnitNext() throws IOException {
		return isKeywordNext(CoordinateReferenceSystemKeyword.TIMEUNIT);
	}

	/**
	 * Read a Geodetic or Geographic CRS
	 * 
	 * @return crs
	 * @throws IOException
	 *             upon failure to read
	 */
	public CoordinateReferenceSystem readGeodeticOrGeographic()
			throws IOException {

		CoordinateReferenceSystem crs = null;

		CoordinateReferenceSystemKeyword type = readKeyword();
		validateKeyword(type, CoordinateReferenceSystemKeyword.GEODCRS,
				CoordinateReferenceSystemKeyword.GEOGCRS);

		readLeftDelimiter();

		String crsName = reader.readQuotedText();

		readSeparator();

		CoordinateReferenceSystemKeyword keyword = peekKeyword();
		switch (keyword) {
		case DATUM:
			GeodeticReferenceFrame geodeticReferenceFrame = readGeodeticReferenceFrame();
			// TODO
			break;
		case ENSEMBLE:
			// TODO
			break;
		case DYNAMIC:
			// TODO
			break;
		default:
			throw new ProjectionException(
					"Unsupported WKT CRS keyword: " + keyword);
		}

		readSeparator();

		CoordinateSystem coordinateSystem = readCoordinateSystem();

		ScopeExtentIdentifierRemark scopeExtentIdentifierRemark = readScopeExtentIdentifierRemark();

		readRightDelimiter();

		return crs; // TODO
	}

	/**
	 * Read a Geodetic reference frame (datum)
	 * 
	 * @return geodetic reference frame
	 * @throws IOException
	 *             upon failure to read
	 */
	public GeodeticReferenceFrame readGeodeticReferenceFrame()
			throws IOException {

		GeodeticReferenceFrame geodeticReferenceFrame = null; // TODO

		CoordinateReferenceSystemKeyword keyword = readKeyword();
		validateKeyword(keyword, CoordinateReferenceSystemKeyword.DATUM);

		readLeftDelimiter();

		String name = reader.readQuotedText();

		readSeparator();

		Ellipsoid ellipsoid = readEllipsoid();

		if (isKeywordNext(CoordinateReferenceSystemKeyword.ANCHOR)) {
			readSeparator();
			String datumAnchor = readKeywordDelimitedQuotedText(
					CoordinateReferenceSystemKeyword.ANCHOR);
			// TODO
		}

		List<Identifier> identifiers = readIdentifiers();

		readRightDelimiter();

		if (isKeywordNext(CoordinateReferenceSystemKeyword.PRIMEM)) {
			readSeparator();
			PrimeMeridian primeMeridian = readPrimeMeridian();
			// TODO
		}

		return geodeticReferenceFrame;
	}

	/**
	 * Read a Prime meridian
	 * 
	 * @return prime meridian
	 * @throws IOException
	 *             upon failure to read
	 */
	public PrimeMeridian readPrimeMeridian() throws IOException {

		PrimeMeridian primeMeridian = null; // TODO

		CoordinateReferenceSystemKeyword keyword = readKeyword();
		validateKeyword(keyword, CoordinateReferenceSystemKeyword.PRIMEM);

		readLeftDelimiter();

		String name = reader.readQuotedText();

		readSeparator();

		double irmLongitude = reader.readNumber();

		if (isKeywordNext(CoordinateReferenceSystemKeyword.ANGLEUNIT)) {
			readSeparator();
			Unit angleUnit = readAngleUnit();
			// TODO
		}

		List<Identifier> identifiers = readIdentifiers();

		readRightDelimiter();

		return primeMeridian;
	}

	/**
	 * Read an Ellipsoid
	 * 
	 * @return ellipsoid
	 * @throws IOException
	 *             upon failure to read
	 */
	public Ellipsoid readEllipsoid() throws IOException {

		Ellipsoid ellipsoid = null; // TODO

		CoordinateReferenceSystemKeyword keyword = readKeyword();
		validateKeyword(keyword, CoordinateReferenceSystemKeyword.ELLIPSOID);

		readLeftDelimiter();

		String name = reader.readQuotedText();

		readSeparator();

		double semiMajorAxis = reader.readUnsignedNumber();

		readSeparator();

		double inverseFlattening = reader.readUnsignedNumber();

		if (isKeywordNext(CoordinateReferenceSystemKeyword.LENGTHUNIT)) {
			readSeparator();
			Unit lengthUnit = readLengthUnit();
			// TODO
		}

		List<Identifier> identifiers = readIdentifiers();

		readRightDelimiter();

		return ellipsoid;
	}

	/**
	 * Read a Unit
	 * 
	 * @return unit
	 * @throws IOException
	 *             upon failure to read
	 */
	public Unit readUnit() throws IOException {
		return readUnit(UnitType.UNIT);
	}

	/**
	 * Read an Angle Unit
	 * 
	 * @return angle unit
	 * @throws IOException
	 *             upon failure to read
	 */
	public Unit readAngleUnit() throws IOException {
		return readUnit(UnitType.ANGLEUNIT);
	}

	/**
	 * Read a Length Unit
	 * 
	 * @return length unit
	 * @throws IOException
	 *             upon failure to read
	 */
	public Unit readLengthUnit() throws IOException {
		return readUnit(UnitType.LENGTHUNIT);
	}

	/**
	 * Read a Parametric Unit
	 * 
	 * @return parametric unit
	 * @throws IOException
	 *             upon failure to read
	 */
	public Unit readParametricUnit() throws IOException {
		return readUnit(UnitType.PARAMETRICUNIT);
	}

	/**
	 * Read a Scale Unit
	 * 
	 * @return scale unit
	 * @throws IOException
	 *             upon failure to read
	 */
	public Unit readScaleUnit() throws IOException {
		return readUnit(UnitType.SCALEUNIT);
	}

	/**
	 * Read a Time Unit
	 * 
	 * @return time unit
	 * @throws IOException
	 *             upon failure to read
	 */
	public Unit readTimeUnit() throws IOException {
		return readUnit(UnitType.TIMEUNIT);
	}

	/**
	 * Read a Unit
	 * 
	 * @param type
	 *            expected unit type
	 * @return unit
	 * @throws IOException
	 *             upon failure to read
	 */
	public Unit readUnit(UnitType type) throws IOException {

		Unit unit = null; // TODO

		Set<CoordinateReferenceSystemKeyword> keywords = readKeywords();
		if (type != UnitType.UNIT) {
			CoordinateReferenceSystemKeyword crsType = CoordinateReferenceSystemKeyword
					.getType(type.name());
			validateKeywords(keywords, crsType);
		} else if (keywords.size() == 1) {
			type = CRSUtils.getUnitType(keywords.iterator().next());
		}

		readLeftDelimiter();

		String name = reader.readQuotedText();

		readSeparator();

		double conversationFactor = reader.readUnsignedNumber();

		List<Identifier> identifiers = readIdentifiers();

		readRightDelimiter();

		return unit;
	}

	/**
	 * Read Identifiers
	 * 
	 * @return identifier
	 * @throws IOException
	 *             upon failure to read
	 */
	public List<Identifier> readIdentifiers() throws IOException {

		List<Identifier> identifiers = new ArrayList<>();

		while (isKeywordNext(CoordinateReferenceSystemKeyword.ID)) {

			readSeparator();

			identifiers.add(readIdentifier());

		}

		return identifiers;
	}

	/**
	 * Read an Identifier
	 * 
	 * @return identifier
	 * @throws IOException
	 *             upon failure to read
	 */
	public Identifier readIdentifier() throws IOException {

		Identifier identifier = null; // TODO

		CoordinateReferenceSystemKeyword keyword = readKeyword();
		validateKeyword(keyword, CoordinateReferenceSystemKeyword.ID);

		readLeftDelimiter();

		String authorityName = reader.readQuotedText();

		readSeparator();

		String authorityUniqueIdentifier = reader.readNumberOrQuotedText();

		if (isNonKeywordNext()) {
			readSeparator();
			String version = reader.readNumberOrQuotedText();
			// TODO
		}

		if (isKeywordNext(CoordinateReferenceSystemKeyword.CITATION)) {
			readSeparator();
			String authorityCitation = readKeywordDelimitedQuotedText(
					CoordinateReferenceSystemKeyword.CITATION);
			// TODO
		}

		if (isKeywordNext(CoordinateReferenceSystemKeyword.URI)) {
			readSeparator();
			String idUri = readKeywordDelimitedQuotedText(
					CoordinateReferenceSystemKeyword.URI);
			// TODO
		}

		readRightDelimiter();

		return identifier;
	}

	/**
	 * Read a Coordinate system
	 * 
	 * @return coordinate system
	 * @throws IOException
	 *             upon failure to read
	 */
	public CoordinateSystem readCoordinateSystem() throws IOException {

		CoordinateSystem coordinateSystem = null; // TODO

		CoordinateReferenceSystemKeyword keyword = readKeyword();
		validateKeyword(keyword, CoordinateReferenceSystemKeyword.CS);

		readLeftDelimiter();

		String csTypeName = reader.readToken();
		CoordinateSystemType csType = CoordinateSystemType.getType(csTypeName);
		if (csType == null) {
			throw new ProjectionException(
					"Unexpected coordinate system type. found: " + csTypeName);
		}

		readSeparator();

		int dimension = reader.readUnsignedInteger();

		List<Identifier> identifiers = readIdentifiers();

		readRightDelimiter();

		boolean isTemporalCountMeasure = CRSUtils
				.isTemporalCountMeasure(csType);

		do {

			readSeparator();

			Axis axis = readAxis(csType);
			// TODO

			if (isTemporalCountMeasure) {
				break;
			}

		} while (isKeywordNext(CoordinateReferenceSystemKeyword.AXIS));

		if (CRSUtils.isSpatial(csType)) {

			if (isUnitNext()) {

				readSeparator();

				Unit unit = readUnit();

				// TODO
			}

		}

		return coordinateSystem;
	}

	/**
	 * Read an Axis
	 * 
	 * @param type
	 *            coordinate system type
	 * @return axis
	 * @throws IOException
	 *             upon failure to read
	 */
	public Axis readAxis(CoordinateSystemType type) throws IOException {

		Axis axis = null; // TODO

		CoordinateReferenceSystemKeyword keyword = readKeyword();
		validateKeyword(keyword, CoordinateReferenceSystemKeyword.AXIS);

		readLeftDelimiter();

		String name = reader.readQuotedText();
		String abbreviation = null;
		if (name.matches("((.+ )|^)\\([a-zA-Z]+\\)$")) {
			int abbrevIndex = name.lastIndexOf("(");
			abbreviation = name.substring(abbrevIndex + 1, name.length() - 1);
			if (abbrevIndex > 0) {
				name = name.substring(0, abbrevIndex - 1);
			} else {
				name = null;
			}
		}

		readSeparator();

		String axisDirectionTypeName = reader.readToken();
		AxisDirectionType axisDirectionType = AxisDirectionType
				.getType(axisDirectionTypeName);
		if (axisDirectionType == null) {
			throw new ProjectionException(
					"Unexpected axis direction type. found: "
							+ axisDirectionTypeName);
		}

		switch (axisDirectionType) {

		case NORTH:
		case SOUTH:

			if (isKeywordNext(CoordinateReferenceSystemKeyword.MERIDIAN)) {

				readSeparator();
				readKeyword();

				readLeftDelimiter();

				double meridian = reader.readNumber();

				readSeparator();

				Unit angleUnit = readAngleUnit();

				readRightDelimiter();

				// TODO
			}

			break;

		case CLOCKWISE:
		case COUNTER_CLOCKWISE:

			readSeparator();

			CoordinateReferenceSystemKeyword bearingKeyword = readKeyword();
			validateKeyword(bearingKeyword,
					CoordinateReferenceSystemKeyword.BEARING);

			readLeftDelimiter();

			double bearing = reader.readNumber();

			readRightDelimiter();

			// TODO

			break;

		default:
		}

		if (isKeywordNext(CoordinateReferenceSystemKeyword.ORDER)) {

			readSeparator();
			readKeyword();

			readLeftDelimiter();

			int axisOrder = reader.readUnsignedInteger();

			readRightDelimiter();

			// TODO
		}

		if (CRSUtils.isSpatial(type)) {

			if (isSpatialUnitNext()) {

				readSeparator();

				Unit unit = readUnit();

				// TODO
			}

		} else if (CRSUtils.isTemporalCountMeasure(type)) {

			if (isTimeUnitNext()) {

				readSeparator();

				Unit unit = readTimeUnit();

				// TODO
			}

		}

		List<Identifier> identifiers = readIdentifiers();

		readRightDelimiter();

		return axis;
	}

	/**
	 * Read a Scope extent identifier remark
	 * 
	 * @return scope extent identifier remark
	 * @throws IOException
	 *             upon failure to read
	 */
	public ScopeExtentIdentifierRemark readScopeExtentIdentifierRemark()
			throws IOException {

		ScopeExtentIdentifierRemark scopeExtentIdentifierRemark = null; // TODO

		// TODO usage

		List<Identifier> identifiers = readIdentifiers();

		// TODO remark

		return scopeExtentIdentifierRemark;
	}

}