package mil.nga.proj.crs.wkt;

import java.io.Closeable;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.nga.proj.crs.Axis;
import mil.nga.proj.crs.CoordinateSystem;
import mil.nga.proj.crs.DatumEnsembleMember;
import mil.nga.proj.crs.Dynamic;
import mil.nga.proj.crs.Ellipsoid;
import mil.nga.proj.crs.Extent;
import mil.nga.proj.crs.GeographicBoundingBox;
import mil.nga.proj.crs.Identifier;
import mil.nga.proj.crs.PrimeMeridian;
import mil.nga.proj.crs.TemporalExtent;
import mil.nga.proj.crs.Unit;
import mil.nga.proj.crs.Usage;
import mil.nga.proj.crs.VerticalExtent;

/**
 * 
 * Well Known Text writer
 * 
 * @author osbornb
 */
public class CRSWriter implements Closeable {

	/**
	 * Logger
	 */
	private static final Logger logger = Logger
			.getLogger(CRSWriter.class.getName());

	/**
	 * Writer
	 */
	private Writer writer;

	/**
	 * Constructor
	 */
	public CRSWriter() {
		this(new StringWriter());
	}

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            writer
	 */
	public CRSWriter(Writer writer) {
		this.writer = writer;
	}

	/**
	 * Get the writer
	 * 
	 * @return writer
	 */
	public Writer getWriter() {
		return writer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return writer.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() {
		try {
			writer.close();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to close writer", e);
		}
	}

	/**
	 * Write a keyword
	 * 
	 * @param keyword
	 *            keyword
	 * @throws IOException
	 *             upon failure to write
	 */
	public void writeKeyword(CoordinateReferenceSystemKeyword keyword)
			throws IOException {
		writer.write(keyword.name());
	}

	/**
	 * Write a left delimiter
	 * 
	 * @throws IOException
	 *             upon failure to write
	 */
	public void writeLeftDelimiter() throws IOException {
		writer.write("[");
	}

	/**
	 * Write a right delimiter
	 * 
	 * @throws IOException
	 *             upon failure to write
	 */
	public void writeRightDelimiter() throws IOException {
		writer.write("]");
	}

	/**
	 * Write a separator
	 * 
	 * @throws IOException
	 *             upon failure to write
	 */
	public void writeSeparator() throws IOException {
		writer.write(",");
	}

	/**
	 * Write the text as quoted
	 * 
	 * @param text
	 *            text
	 * @throws IOException
	 *             upon failure to write
	 */
	public void writeQuotedText(String text) throws IOException {
		writer.write("\"");
		writer.write(text);
		writer.write("\"");
	}

	/**
	 * Write a number or quoted text if not a number
	 * 
	 * @param number
	 *            number
	 * @throws IOException
	 *             upon failure to write
	 */
	public void write(Number number) throws IOException {
		writer.write(number.toString());
	}

	/**
	 * Write a number or quoted text if not a number
	 * 
	 * @param text
	 *            text
	 * @throws IOException
	 *             upon failure to write
	 */
	public void writeNumberOrQuotedText(String text) throws IOException {
		try {
			Double.parseDouble(text);
			writer.write(text);
		} catch (NumberFormatException e) {
			writeQuotedText(text);
		}
	}

	/**
	 * Write a keyword delimited text
	 * 
	 * @param keyword
	 *            keyword
	 * @param text
	 *            text
	 * @throws IOException
	 *             upon failure to write
	 */
	public void writeKeywordDelimitedQuotedText(
			CoordinateReferenceSystemKeyword keyword, String text)
			throws IOException {

		writeKeyword(keyword);

		writeLeftDelimiter();

		writeQuotedText(text);

		writeRightDelimiter();
	}

	// TODO

	/**
	 * Write a datum ensemble member to well-known text
	 * 
	 * @param datumEnsembleMember
	 *            datum ensemble member
	 * @throws IOException
	 *             upon failure to write
	 */
	public void write(DatumEnsembleMember datumEnsembleMember)
			throws IOException {

		writeKeyword(CoordinateReferenceSystemKeyword.MEMBER);

		writeLeftDelimiter();

		writeQuotedText(datumEnsembleMember.getName());

		if (datumEnsembleMember.hasIdentifiers()) {
			writeSeparator();
			writeIdentifiers(datumEnsembleMember.getIdentifiers());
		}

		writeRightDelimiter();
	}

	/**
	 * Write a dynamic to well-known text
	 * 
	 * @param dynamic
	 *            dynamic
	 * @throws IOException
	 *             upon failure to write
	 */
	public void write(Dynamic dynamic) throws IOException {

		writeKeyword(CoordinateReferenceSystemKeyword.DYNAMIC);

		writeLeftDelimiter();

		writeKeyword(CoordinateReferenceSystemKeyword.FRAMEEPOCH);

		writeLeftDelimiter();

		write(dynamic.getReferenceEpoch());

		writeRightDelimiter();

		if (dynamic.hasDeformationModelName()) {

			writeSeparator();
			writeKeyword(CoordinateReferenceSystemKeyword.MODEL);

			writeLeftDelimiter();

			writeQuotedText(dynamic.getDeformationModelName());

			if (dynamic.hasDeformationModelIdentifiers()) {
				writeSeparator();
				writeIdentifiers(dynamic.getDeformationModelIdentifiers());
			}

			writeRightDelimiter();
		}

		writeRightDelimiter();
	}

	/**
	 * Write a prime meridian to well-known text
	 * 
	 * @param primeMeridian
	 *            prime meridian
	 * @throws IOException
	 *             upon failure to write
	 */
	public void write(PrimeMeridian primeMeridian) throws IOException {

		writeKeyword(CoordinateReferenceSystemKeyword.PRIMEM);

		writeLeftDelimiter();

		writeQuotedText(primeMeridian.getName());

		writeSeparator();

		write(primeMeridian.getIrmLongitude());

		if (primeMeridian.hasIrmLongitudeAngleUnit()) {
			writeSeparator();
			write(primeMeridian.getIrmLongitudeAngleUnit());
		}

		if (primeMeridian.hasIdentifiers()) {
			writeSeparator();
			writeIdentifiers(primeMeridian.getIdentifiers());
		}

		writeRightDelimiter();
	}

	/**
	 * Write an ellipsoid to well-known text
	 * 
	 * @param ellipsoid
	 *            ellipsoid
	 * @throws IOException
	 *             upon failure to write
	 */
	public void write(Ellipsoid ellipsoid) throws IOException {

		writeKeyword(CoordinateReferenceSystemKeyword.ELLIPSOID);

		writeLeftDelimiter();

		writeQuotedText(ellipsoid.getName());

		writeSeparator();

		write(ellipsoid.getSemiMajorAxis());

		writeSeparator();

		write(ellipsoid.getInverseFlattening());

		if (ellipsoid.hasLengthUnit()) {
			writeSeparator();
			write(ellipsoid.getLengthUnit());
		}

		if (ellipsoid.hasIdentifiers()) {
			writeSeparator();
			writeIdentifiers(ellipsoid.getIdentifiers());
		}

		writeRightDelimiter();
	}

	/**
	 * Write a unit to well-known text
	 * 
	 * @param unit
	 *            unit
	 * @throws IOException
	 *             upon failure to write
	 */
	public void write(Unit unit) throws IOException {

		writer.write(unit.getType().name());

		writeLeftDelimiter();

		writeQuotedText(unit.getName());

		if (unit.hasConversionFactor()) {
			writeSeparator();
			write(unit.getConversionFactor());
		}

		if (unit.hasIdentifiers()) {
			writeSeparator();
			writeIdentifiers(unit.getIdentifiers());
		}

		writeRightDelimiter();
	}

	/**
	 * Write identifiers to well-known text
	 * 
	 * @param identifiers
	 *            identifiers
	 * @throws IOException
	 *             upon failure to write
	 */
	public void writeIdentifiers(List<Identifier> identifiers)
			throws IOException {

		for (int i = 0; i < identifiers.size(); i++) {

			if (i > 0) {
				writeSeparator();
			}

			write(identifiers.get(i));
		}

	}

	/**
	 * Write an identifier to well-known text
	 * 
	 * @param identifier
	 *            identifier
	 * @throws IOException
	 *             upon failure to write
	 */
	public void write(Identifier identifier) throws IOException {

		writeKeyword(CoordinateReferenceSystemKeyword.ID);

		writeLeftDelimiter();

		writeQuotedText(identifier.getName());

		writeSeparator();

		writeNumberOrQuotedText(identifier.getUniqueIdentifier());

		if (identifier.hasVersion()) {
			writeSeparator();
			writeNumberOrQuotedText(identifier.getVersion());
		}

		if (identifier.hasCitation()) {
			writeSeparator();
			writeKeywordDelimitedQuotedText(
					CoordinateReferenceSystemKeyword.CITATION,
					identifier.getCitation());
		}

		if (identifier.hasUri()) {
			writeSeparator();
			writeKeywordDelimitedQuotedText(
					CoordinateReferenceSystemKeyword.URI, identifier.getUri());
		}

		writeRightDelimiter();
	}

	/**
	 * Write a coordinate system to well-known text
	 * 
	 * @param coordinateSystem
	 *            coordinate system
	 * @throws IOException
	 *             upon failure to write
	 */
	public void write(CoordinateSystem coordinateSystem) throws IOException {

		writeKeyword(CoordinateReferenceSystemKeyword.CS);

		writeLeftDelimiter();

		writer.write(coordinateSystem.getType().getName());

		writeSeparator();

		write(coordinateSystem.getDimension());

		if (coordinateSystem.hasIdentifiers()) {
			writeSeparator();
			writeIdentifiers(coordinateSystem.getIdentifiers());
		}

		writeRightDelimiter();

		for (Axis axis : coordinateSystem.getAxes()) {
			writeSeparator();
			write(axis);
		}

		if (coordinateSystem.hasUnit()) {
			writeSeparator();
			write(coordinateSystem.getUnit());
		}

	}

	/**
	 * Write an axis to well-known text
	 * 
	 * @param axis
	 *            axis
	 * @throws IOException
	 *             upon failure to write
	 */
	public void write(Axis axis) throws IOException {

		writeKeyword(CoordinateReferenceSystemKeyword.AXIS);

		writeLeftDelimiter();

		StringBuilder nameAbbrev = new StringBuilder();
		if (axis.hasName()) {
			nameAbbrev.append(axis.getName());
		}
		if (axis.hasAbbreviation()) {
			if (nameAbbrev.length() > 0) {
				nameAbbrev.append(" ");
			}
			nameAbbrev.append("(");
			nameAbbrev.append(axis.getAbbreviation());
			nameAbbrev.append(")");
		}
		writeQuotedText(nameAbbrev.toString());

		writeSeparator();

		writer.write(axis.getDirection().getName());

		switch (axis.getDirection()) {

		case NORTH:
		case SOUTH:

			if (axis.hasMeridian()) {

				writeSeparator();
				writeKeyword(CoordinateReferenceSystemKeyword.MERIDIAN);

				writeLeftDelimiter();

				write(axis.getMeridian());

				writeSeparator();

				write(axis.getMeridianAngleUnit());

				writeRightDelimiter();
			}

			break;

		case CLOCKWISE:
		case COUNTER_CLOCKWISE:

			writeSeparator();
			writeKeyword(CoordinateReferenceSystemKeyword.BEARING);

			writeLeftDelimiter();

			write(axis.getBearing());

			writeRightDelimiter();

			break;

		default:
		}

		if (axis.hasOrder()) {

			writeSeparator();
			writeKeyword(CoordinateReferenceSystemKeyword.ORDER);

			writeLeftDelimiter();

			write(axis.getOrder());

			writeRightDelimiter();
		}

		if (axis.hasUnit()) {
			writeSeparator();
			write(axis.getUnit());
		}

		if (axis.hasIdentifiers()) {
			writeSeparator();
			writeIdentifiers(axis.getIdentifiers());
		}

		writeRightDelimiter();
	}

	/**
	 * Write a remark to well-known text
	 * 
	 * @param remark
	 *            remark
	 * @throws IOException
	 *             upon failure to write
	 */
	public void writeRemark(String remark) throws IOException {
		writeKeywordDelimitedQuotedText(CoordinateReferenceSystemKeyword.REMARK,
				remark);
	}

	/**
	 * Write usages to well-known text
	 * 
	 * @param usages
	 *            usages
	 * @throws IOException
	 *             upon failure to write
	 */
	public void writeUsages(List<Usage> usages) throws IOException {

		for (int i = 0; i < usages.size(); i++) {

			if (i > 0) {
				writeSeparator();
			}

			write(usages.get(i));
		}

	}

	/**
	 * Write a usage to well-known text
	 * 
	 * @param usage
	 *            usage
	 * @throws IOException
	 *             upon failure to write
	 */
	public void write(Usage usage) throws IOException {

		writeKeyword(CoordinateReferenceSystemKeyword.USAGE);

		writeLeftDelimiter();

		writeScope(usage.getScope());

		write(usage.getExtent());

		writeRightDelimiter();
	}

	/**
	 * Write a scope to well-known text
	 * 
	 * @param scope
	 *            scope
	 * @throws IOException
	 *             upon failure to write
	 */
	public void writeScope(String scope) throws IOException {
		writeKeywordDelimitedQuotedText(CoordinateReferenceSystemKeyword.SCOPE,
				scope);
	}

	/**
	 * Write an extent to well-known text
	 * 
	 * @param extent
	 *            extent
	 * @throws IOException
	 *             upon failure to write
	 */
	public void write(Extent extent) throws IOException {

		if (extent.hasAreaDescription()) {
			writeSeparator();
			writeAreaDescription(extent.getAreaDescription());
		}

		if (extent.hasGeographicBoundingBox()) {
			writeSeparator();
			write(extent.getGeographicBoundingBox());
		}

		if (extent.hasVerticalExtent()) {
			writeSeparator();
			write(extent.getVerticalExtent());
		}

		if (extent.hasTemporalExtent()) {
			writeSeparator();
			write(extent.getTemporalExtent());
		}

	}

	/**
	 * Write an area description to well-known text
	 * 
	 * @param areaDescription
	 *            area description
	 * @throws IOException
	 *             upon failure to write
	 */
	public void writeAreaDescription(String areaDescription)
			throws IOException {
		writeKeywordDelimitedQuotedText(CoordinateReferenceSystemKeyword.AREA,
				areaDescription);
	}

	/**
	 * Write a geographic bounding box to well-known text
	 * 
	 * @param geographicBoundingBox
	 *            geographic bounding box
	 * @throws IOException
	 *             upon failure to write
	 */
	public void write(GeographicBoundingBox geographicBoundingBox)
			throws IOException {

		writeKeyword(CoordinateReferenceSystemKeyword.BBOX);

		writeLeftDelimiter();

		write(geographicBoundingBox.getLowerLeftLatitude());

		writeSeparator();

		write(geographicBoundingBox.getLowerLeftLongitude());

		writeSeparator();

		write(geographicBoundingBox.getUpperRightLatitude());

		writeSeparator();

		write(geographicBoundingBox.getUpperRightLongitude());

		writeRightDelimiter();
	}

	/**
	 * Write a vertical extent to well-known text
	 * 
	 * @param verticalExtent
	 *            vertical extent
	 * @throws IOException
	 *             upon failure to write
	 */
	public void write(VerticalExtent verticalExtent) throws IOException {

		writeKeyword(CoordinateReferenceSystemKeyword.VERTICALEXTENT);

		writeLeftDelimiter();

		write(verticalExtent.getMinimumHeight());

		writeSeparator();

		write(verticalExtent.getMaximumHeight());

		if (verticalExtent.hasLengthUnit()) {
			writeSeparator();
			write(verticalExtent.getLengthUnit());
		}

		writeRightDelimiter();
	}

	/**
	 * Write a temporal extent to well-known text
	 * 
	 * @param temporalExtent
	 *            temporal extent
	 * @throws IOException
	 *             upon failure to write
	 */
	public void write(TemporalExtent temporalExtent) throws IOException {

		writeKeyword(CoordinateReferenceSystemKeyword.TIMEEXTENT);

		writeLeftDelimiter();

		writeQuotedText(temporalExtent.getStart()); // TODO date vs quoted text

		writeSeparator();

		writeQuotedText(temporalExtent.getEnd());

		writeRightDelimiter();
	}

}
