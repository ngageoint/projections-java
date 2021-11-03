package mil.nga.proj;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.locationtech.proj4j.ProjCoordinate;

/**
 * Transform coordinates between projections
 * 
 * To run from command line, build with the standalone profile:
 * 
 * mvn clean install -Pstandalone
 * 
 * java -jar name.jar +usage_arguments
 * 
 * java -classpath name.jar mil.nga.proj.Project +usage_arguments
 * 
 * @author osbornb
 * @since 1.1.0
 */
public class Project {

	/**
	 * Help Argument
	 */
	private static final String HELP_ARG = "-help";

	/**
	 * From Projection Prompt
	 */
	private static final String FROM_PROMPT = "From Projection: ";

	/**
	 * To Projection Prompt
	 */
	private static final String TO_PROMPT = "To Projection: ";

	/**
	 * Coordinates Prompt
	 */
	private static final String COORDINATES_PROMPT = "Coordinate(s): ";

	/**
	 * Inverse command
	 */
	private static final String INVERSE_COMMAND = "Inverse";

	/**
	 * Projection Transform
	 */
	private static ProjectionTransform transform = null;

	/**
	 * Main method to generate tiles in a GeoPackage
	 * 
	 * @param args
	 *            arguments
	 */
	public static void main(String[] args) {

		boolean valid = true;
		Projection fromProjection = null;
		StringBuilder builder = null;

		for (int i = 0; i < args.length; i++) {

			String arg = args[i];

			if (arg.equalsIgnoreCase(HELP_ARG)) {
				valid = false;
				break;
			}

			switch (i) {
			case 0:
				fromProjection = createProjection(arg);
				valid = false;
				break;
			case 1:
				Projection toProjection = createProjection(arg);
				if (fromProjection != null && toProjection != null) {
					transform = fromProjection.getTransformation(toProjection);
					valid = true;
				}
				break;
			default:
				if (builder == null) {
					builder = new StringBuilder();
				} else {
					builder.append(" ");
				}
				builder.append(arg);
				break;
			}

		}

		List<ProjCoordinate> coordinates = null;
		if (builder != null) {
			coordinates = createCoordinates(builder.toString());
			valid = !coordinates.isEmpty();
		}

		if (!valid) {
			printUsage();
		} else {
			if (coordinates == null) {
				commandPrompt();
			} else {
				printTransform();
				project(coordinates);
				System.out.println();
			}
		}

	}

	/**
	 * Create a projection
	 * 
	 * @param value
	 *            projection value
	 * @return projection
	 */
	private static Projection createProjection(String value) {

		Projection projection = null;

		try {

			if (value.contains("[") && value.contains("]")) {
				projection = ProjectionFactory.getProjectionByDefinition(value);
			} else if (value.contains("+")) {
				projection = ProjectionFactory.getProjectionByParams(value);
			} else {
				String authority = ProjectionConstants.AUTHORITY_EPSG;
				String code = value;
				String[] parts = code.split(":");
				if (parts.length == 2) {
					authority = parts[0];
					code = parts[1];
				}
				projection = ProjectionFactory.getProjection(authority, code);
			}

		} catch (Exception e) {

		}

		if (projection == null) {
			System.out.println("Error: Invalid projection '" + value + "'");
		}

		return projection;
	}

	/**
	 * Create coordinates
	 * 
	 * @param value
	 *            coordinate values
	 * @return projection coordinates
	 */
	private static List<ProjCoordinate> createCoordinates(String value) {

		List<ProjCoordinate> coordinates = new ArrayList<>();

		if (value != null) {
			value = value.trim();

			String[] parts = null;
			if (value.startsWith("[")) {
				parts = value.split("\\[|\\]");
			} else if (value.contains(",")) {
				value = value.replaceAll("\\s*,\\s*", ",");
				parts = value.split("\\s+");
			} else {
				parts = new String[] { value };
			}

			for (String part : parts) {
				part = part.trim();
				if (!part.isEmpty() && !part.equals(",")) {
					ProjCoordinate coordinate = createCoordinate(part);
					if (coordinate != null) {
						coordinates.add(coordinate);
					}
				}
			}

		}
		return coordinates;
	}

	/**
	 * Create a coordinate
	 * 
	 * @param value
	 *            coordinate value
	 * @return projection coordinate
	 */
	private static ProjCoordinate createCoordinate(String value) {

		ProjCoordinate coordinate = null;

		String[] parts = value.split("\\s*,\\s*|\\s+");
		if (parts.length == 2 || parts.length == 3) {
			try {
				double x = Double.parseDouble(parts[0]);
				double y = Double.parseDouble(parts[1]);
				if (parts.length == 3) {
					double z = Double.parseDouble(parts[2]);
					coordinate = new ProjCoordinate(x, y, z);
				} else {
					coordinate = new ProjCoordinate(x, y);
				}
			} catch (Exception e) {

			}
		}

		if (coordinate == null) {
			System.out.println("Error: Invalid coordinate '" + value + "'");
		}

		return coordinate;
	}

	/**
	 * Project coordinates
	 * 
	 * @param coordinates
	 *            coordinates
	 */
	public static void project(List<ProjCoordinate> coordinates) {

		System.out.println();

		for (ProjCoordinate coordinate : coordinates) {

			System.out.print(coordinate.toShortString() + " -> ");

			ProjCoordinate transformed = transform.transform(coordinate);
			System.out.println(transformed.toShortString());

		}

	}

	/**
	 * Command prompt accepting projections and/or coordinates
	 */
	private static void commandPrompt() {

		Scanner scanner = new Scanner(System.in);

		try {

			if (transform == null) {

				Projection fromProjection = null;

				System.out.println();
				System.out.print(FROM_PROMPT);

				StringBuilder proj = new StringBuilder();
				int openCount = 0;
				int closeCount = 0;

				while (scanner.hasNextLine()) {

					String input = scanner.nextLine().trim();
					proj.append(input);

					openCount += input.length()
							- input.replaceAll("\\[", "").length();
					if (openCount > 0) {
						closeCount += input.length()
								- input.replaceAll("\\]", "").length();
					}

					if (closeCount >= openCount) {

						try {

							if (fromProjection == null) {
								fromProjection = createProjection(
										proj.toString());
							} else {
								Projection toProjection = createProjection(
										proj.toString());
								transform = fromProjection
										.getTransformation(toProjection);
								break;
							}

						} catch (Exception e) {

						}

						proj = new StringBuilder();
						openCount = 0;
						closeCount = 0;

						System.out.println();
						if (fromProjection == null) {
							System.out.print(FROM_PROMPT);
						} else {
							System.out.print(TO_PROMPT);
						}

					}

				}

			}

			printTransform();

			commandPrompt(scanner);

		} finally {
			scanner.close();
		}

	}

	/**
	 * Command prompt accepting projections and/or coordinates
	 */
	private static void commandPrompt(Scanner scanner) {

		System.out.println();
		System.out.print(COORDINATES_PROMPT);

		while (scanner.hasNextLine()) {

			try {

				String line = scanner.nextLine();

				if (line != null) {

					line = line.trim();

					if (line.equalsIgnoreCase(INVERSE_COMMAND)) {

						transform = transform.getInverseTransformation();

						printTransform();

					} else {

						List<ProjCoordinate> coordinates = createCoordinates(
								line);

						if (!coordinates.isEmpty()) {
							project(coordinates);
						}

					}

				}

			} catch (Exception e) {

			}

			System.out.println();
			System.out.print(COORDINATES_PROMPT);

		}

	}

	/**
	 * Print the transform
	 */
	private static void printTransform() {

		Projection from = transform.getFromProjection();
		Projection to = transform.getToProjection();

		String fromCode = from.getCode();
		String toCode = to.getCode();

		System.out.println();
		if (!fromCode.isEmpty() && !toCode.isEmpty()) {
			System.out.println(from + " -> " + to);
		} else {
			if (!fromCode.isEmpty()) {
				System.out.println(from);
			} else if (from.getDefinition() != null) {
				System.out.println(from.getDefinition());
			} else {
				System.out.println(from.getCrs().getParameterString());
			}
			System.out.println("->");
			if (!toCode.isEmpty()) {
				System.out.println(to);
			} else if (to.getDefinition() != null) {
				System.out.println(to.getDefinition());
			} else {
				System.out.println(to.getCrs().getParameterString());
			}
		}

	}

	/**
	 * Print usage for the main method
	 */
	private static void printUsage() {
		System.out.println();
		System.out.println("USAGE");
		System.out.println();
		System.out.println("\t[from_projection to_projection [coordinates]]");
		System.out.println();
		System.out.println("DESCRIPTION");
		System.out.println();
		System.out.println("\tTransform coordinates between projections");
		System.out.println();
		System.out.println("\tfrom_projection");
		System.out.println("\t\tTODO");
		System.out.println();
		System.out.println("\tto_projection");
		System.out.println("\t\tTODO");
		System.out.println();
		System.out.println("\tcoordinates");
		System.out.println("\t\tTODO");
		System.out.println();
	}

}
