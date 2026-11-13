import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Renders the Taxify UML class diagram to PNG with Java2D.
 * Regenerate from the repo root: java docs/diagram/ClassDiagram.java docs/class-diagram.png
 */
public class ClassDiagram {

    enum Kind { INTERFACE, CLASS, ABSTRACT, ENUM }

    enum EdgeType { GENERALIZATION, REALIZATION, ASSOCIATION, DEPENDENCY }

    static final class Box {
        final String name;
        final Kind kind;
        final String pkg;
        final List<String> fields;
        final List<String> methods;
        double x, y, w, h, headerH, fieldsH;

        Box(String name, Kind kind, String pkg, List<String> fields, List<String> methods) {
            this.name = name;
            this.kind = kind;
            this.pkg = pkg;
            this.fields = fields;
            this.methods = methods;
        }

        double cx() { return x + w / 2; }
        double cy() { return y + h / 2; }
        double right() { return x + w; }
        double bottom() { return y + h; }
        Point2D top(double f) { return new Point2D.Double(x + w * f, y); }
        Point2D bot(double f) { return new Point2D.Double(x + w * f, y + h); }
        Point2D left(double f) { return new Point2D.Double(x, y + h * f); }
        Point2D rightAt(double f) { return new Point2D.Double(x + w, y + h * f); }
    }

    record Label(String text, double x, double y, int align) { }

    record Edge(EdgeType type, List<Point2D> points, List<Label> labels) { }

    // ---- style -------------------------------------------------------------------------
    static final Font NAME = new Font("Segoe UI", Font.BOLD, 15);
    static final Font NAME_ITALIC = new Font("Segoe UI", Font.BOLD | Font.ITALIC, 15);
    static final Font STEREO = new Font("Segoe UI", Font.ITALIC, 11);
    static final Font MEMBER = new Font("Consolas", Font.PLAIN, 12);
    static final Font PKG = new Font("Segoe UI", Font.BOLD, 14);
    static final Font LABEL = new Font("Segoe UI", Font.PLAIN, 12);
    static final Font TITLE = new Font("Segoe UI", Font.BOLD, 26);
    static final Font SUBTITLE = new Font("Segoe UI", Font.PLAIN, 14);

    static final Color INK = new Color(0x1F2328);
    static final Color MUTED = new Color(0x59636E);
    static final Color LINE = new Color(0x4B5563);
    static final Color BG = Color.WHITE;

    static Color headerFill(Kind k) {
        return switch (k) {
            case INTERFACE -> new Color(0xE3EDFD);
            case CLASS -> new Color(0xE3F4E8);
            case ABSTRACT -> new Color(0xEFE6FB);
            case ENUM -> new Color(0xFDF0DC);
        };
    }

    static Color accent(Kind k) {
        return switch (k) {
            case INTERFACE -> new Color(0x3B6FD4);
            case CLASS -> new Color(0x2F8A50);
            case ABSTRACT -> new Color(0x7E4FC2);
            case ENUM -> new Color(0xC07A12);
        };
    }

    static final Map<String, Color> PKG_FILL = Map.of(
        "simulation", new Color(0xF7F8FA),
        "company", new Color(0xF7F8FA),
        "people", new Color(0xF7F8FA),
        "vehicle", new Color(0xF7F8FA),
        "map", new Color(0xF7F8FA));

    static final double LINE_H = 16;
    static final double PAD_X = 12;
    static final double SECTION_PAD = 6;

    static final Map<String, Box> boxes = new LinkedHashMap<>();
    static final List<Edge> edges = new ArrayList<>();
    static FontMetrics memberFm, nameFm, stereoFm;

    static Box box(String name, Kind kind, String pkg, String[] fields, String... methods) {
        Box b = new Box(name, kind, pkg, Arrays.asList(fields), Arrays.asList(methods));
        boxes.put(name, b);
        return b;
    }

    static String[] f(String... s) { return s; }

    static final String[] NONE = {};

    // ---- model ---------------------------------------------------------------------------
    static void defineModel() {
        // simulation
        box("Main", Kind.CLASS, "simulation",
            f("- REQUEST_TICKS: int = 60", "- MAX_DRAIN_TICKS: int = 500"),
            "_+ main(args: String[]): void",
            "_- issueRandomRequest(sim: IApplicationSimulator): void");
        box("IApplicationSimulator", Kind.INTERFACE, "simulation", NONE,
            "+ show(): void",
            "+ showStatistics(): void",
            "+ update(): void",
            "+ requestService(pinkRide: boolean, rideMode: RideMode): boolean",
            "+ requestRentalService(type: RentalVehicleType): boolean",
            "+ allVehiclesFree(): boolean",
            "+ getTotalServices(): int");
        box("ApplicationSimulator", Kind.CLASS, "simulation",
            f("- company: ITaxiCompany", "- users: List<IUser>", "- vehicles: List<IVehicle>"),
            "+ ApplicationSimulator(company, users, vehicles)",
            "+ updateObserver(message: String): void",
            "- randomFreeUser(pinkRide: boolean): IUser");
        box("ApplicationLibrary", Kind.CLASS, "simulation",
            f("_+ MINIMUM_DISTANCE: int = 3", "_+ MAP_WIDTH: int = 10", "_+ MAP_HEIGHT: int = 10", "_- random: Random"),
            "_+ setSeed(seed: long): void",
            "_+ rand(): int",
            "_+ rand(max: int): int",
            "_+ chance(): double",
            "_+ distance(a: ILocation, b: ILocation): int",
            "_+ randomLocation(): ILocation",
            "_+ randomLocation(from: ILocation): ILocation");

        // company
        box("ISubject", Kind.INTERFACE, "company", NONE,
            "+ addObserver(observer: IObserver): void",
            "+ notifyObserver(message: String): void");
        box("IObserver", Kind.INTERFACE, "company", NONE,
            "+ updateObserver(message: String): void");
        box("ITaxiCompany", Kind.INTERFACE, "company", NONE,
            "+ getName(): String",
            "+ getTotalServices(): int",
            "+ provideService(userId: int, pinkRide: boolean, rideMode: RideMode): boolean",
            "+ provideRentalService(userId: int, type: RentalVehicleType): boolean",
            "+ arrivedAtPickupLocation(vehicle: IVehicle): void",
            "+ arrivedAtDropoffLocation(vehicle: IVehicle): void",
            "+ addObserver(observer: IObserver): void",
            "+ notifyObserver(message: String): void",
            "+ findNearestRental(type: RentalVehicleType, location: ILocation): int");
        box("TaxiCompany", Kind.CLASS, "company",
            f("_+ MAX_SHARED_RIDERS: int = 2", "- name: String", "- users: List<IUser>", "- vehicles: List<IVehicle>",
              "- totalServices: int", "- observer: IObserver"),
            "+ TaxiCompany(name, users, vehicles)",
            "- findFreeVehicle(pinkRide: boolean): IVehicle",
            "- findSharableVehicle(pinkRide: boolean, rideMode: RideMode): IVehicle",
            "- everyoneAcceptsSharing(user: IUser, service: IService): boolean",
            "- driverAllowed(vehicle: IVehicle, pinkRide: boolean): boolean",
            "- findUser(id: int): IUser");
        box("IService", Kind.INTERFACE, "company", NONE,
            "+ getUsers(): List<IUser>",
            "+ addUser(user: IUser): void",
            "+ getPickupLocation(): ILocation",
            "+ getDropoffLocation(): ILocation",
            "+ getStars(): int",
            "+ setStars(stars: int): void",
            "+ calculateDistance(): int",
            "+ isPinkRide(): boolean",
            "+ getRideMode(): RideMode");
        box("Service", Kind.CLASS, "company",
            f("- users: List<IUser>", "- pickup: ILocation", "- dropoff: ILocation", "- stars: int",
              "- isPink: boolean", "- rideMode: RideMode"),
            "+ Service(user, pickup, dropoff, pinkRide, rideMode)");
        box("IStatistics", Kind.INTERFACE, "company", NONE,
            "+ getServices(): int",
            "+ getReviews(): int",
            "+ getStars(): double",
            "+ getDistance(): int",
            "+ getBilling(): double",
            "+ updateServices(): void",
            "+ updateReviews(): void",
            "+ updateStars(stars: int): void",
            "+ updateDistance(distance: int): void",
            "+ updateBilling(billing: double): void");
        box("Statistics", Kind.CLASS, "company",
            f("- services: int", "- ratings: int", "- stars: int", "- distance: int", "- billing: double"),
            "+ Statistics()");
        box("RideMode", Kind.ENUM, "company", f("STANDARD", "SILENT"));

        // people
        box("Person", Kind.CLASS, "people",
            f("- firstName: String", "- lastName: String", "- gender: char", "- birthDate: LocalDate"),
            "+ Person(firstName, lastName, gender, birthDate)",
            "+ getFirstName(): String",
            "+ getLastName(): String",
            "+ getName(): String",
            "+ setName(name: String): void",
            "+ getGender(): char",
            "+ setGender(gender: char): void",
            "+ getBirthDate(): LocalDate",
            "+ setBirthDate(birthDate: LocalDate): void");
        box("IUser", Kind.INTERFACE, "people", NONE,
            "+ getId(): int",
            "+ getService(): boolean",
            "+ setService(service: boolean): void",
            "+ setCompany(company: ITaxiCompany): void",
            "+ requestService(pinkRide: boolean, rideMode: RideMode): void",
            "+ requestRentalService(type: RentalVehicleType): void",
            "+ rateService(service: IService): void",
            "+ acceptSharedRide(): boolean",
            "+ isEligibleForPinkRide(): boolean");
        box("User", Kind.CLASS, "people",
            f("- id: int", "- company: ITaxiCompany", "- service: boolean"),
            "+ User(id, firstName, lastName, gender, birthDate)");
        box("IDriver", Kind.INTERFACE, "people", NONE,
            "+ getYearsOfExperience(): int",
            "+ setYearsOfExperience(years: int): void",
            "+ getRating(): double",
            "+ setRating(rating: double): void");
        box("Driver", Kind.CLASS, "people",
            f("- yearsOfExperience: int", "- rating: double"),
            "+ Driver()",
            "+ Driver(name, gender, birthDate, yearsOfExperience, rating)");

        // vehicle
        box("IMovable", Kind.INTERFACE, "vehicle", NONE, "+ move(): void");
        box("IVehicle", Kind.INTERFACE, "vehicle", NONE,
            "+ getId(): int",
            "+ getLocation(): ILocation",
            "+ getDestination(): ILocation",
            "+ getService(): IService",
            "+ getStatistics(): IStatistics",
            "+ getDriver(): Person",
            "+ setDriver(driver: Person): void",
            "+ getCompany(): ITaxiCompany",
            "+ setCompany(company: ITaxiCompany): void",
            "+ pickService(service: IService): void",
            "+ startService(): void",
            "+ endService(riders: int): void",
            "+ notifyArrivalAtPickupLocation(): void",
            "+ notifyArrivalAtDropoffLocation(): void",
            "+ isFree(): boolean",
            "+ calculateCost(): double");
        String[] vehicleFields = {"- id: int", "- company: ITaxiCompany", "- service: IService", "- status: VehicleStatus",
            "- location: ILocation", "- destination: ILocation", "- statistics: IStatistics", "- route: IRoute",
            "- driver: Person"};
        box("Vehicle", Kind.ABSTRACT, "vehicle", vehicleFields,
            "+ Vehicle(id, location, driver)",
            "+ getStatus(): VehicleStatus",
            "+ move(): void",
            "+ calculateCost(): double");
        box("Taxi", Kind.CLASS, "vehicle", NONE,
            "+ Taxi(id, location, company, driver)",
            "+ calculateCost(): double");
        box("Shuttle", Kind.CLASS, "vehicle", NONE,
            "+ Shuttle(id, location, company, driver)",
            "+ calculateCost(): double");
        String[] microFields = vehicleFields.clone();
        microFields[3] = "- status: MicroVehicleStatus";
        box("MicroVehicle", Kind.CLASS, "vehicle", microFields,
            "+ MicroVehicle(id, location, driver)",
            "+ getStatus(): MicroVehicleStatus",
            "+ getRentalType(): RentalVehicleType",
            "+ move(): void",
            "+ calculateCost(): double");
        box("Bike", Kind.CLASS, "vehicle",
            f("- rentalType: RentalVehicleType = BIKE"),
            "+ Bike(id, location, driver, company)",
            "+ calculateCost(): double",
            "+ getRentalType(): RentalVehicleType");
        box("Scooter", Kind.CLASS, "vehicle",
            f("- battery: double = 100", "- rentalType: RentalVehicleType = SCOOTER"),
            "+ Scooter(id, location, driver, company)",
            "+ getBattery(): double",
            "+ chargeBattery(): void",
            "+ startService(): void",
            "+ endService(riders: int): void",
            "+ calculateCost(): double",
            "+ getRentalType(): RentalVehicleType");
        box("VehicleStatus", Kind.ENUM, "vehicle", f("FREE", "PICKUP", "SERVICE"));
        box("MicroVehicleStatus", Kind.ENUM, "vehicle", f("FREE", "BOOKED", "RIDING"));
        box("RentalVehicleType", Kind.ENUM, "vehicle", f("SCOOTER", "BIKE"));

        // map
        box("ILocation", Kind.INTERFACE, "map", NONE, "+ getX(): int", "+ getY(): int");
        box("Location", Kind.CLASS, "map", f("- x: int", "- y: int"), "+ Location(x: int, y: int)");
        box("IRoute", Kind.INTERFACE, "map", NONE, "+ hasLocations(): boolean", "+ getNextLocation(): ILocation");
        box("Route", Kind.CLASS, "map", f("- route: List<ILocation>"),
            "+ Route(from: ILocation, to: ILocation)",
            "_- setRoute(from: ILocation, to: ILocation): List<ILocation>");
    }

    static String stereotype(Kind k) {
        return switch (k) {
            case INTERFACE -> "«interface»";
            case ENUM -> "«enumeration»";
            case ABSTRACT -> "«abstract»";
            case CLASS -> null;
        };
    }

    static String plain(String member) {
        return member.startsWith("_") ? member.substring(1) : member;
    }

    static void measure() {
        for (Box b : boxes.values()) {
            double w = nameFm.stringWidth(b.name);
            String st = stereotype(b.kind);
            if (st != null) {
                w = Math.max(w, stereoFm.stringWidth(st));
            }
            for (String m : b.fields) {
                w = Math.max(w, memberFm.stringWidth(plain(m)));
            }
            for (String m : b.methods) {
                w = Math.max(w, memberFm.stringWidth(plain(m)));
            }
            b.w = Math.max(150, Math.ceil(w + 2 * PAD_X));
            b.headerH = st != null ? 44 : 32;
            b.fieldsH = b.fields.isEmpty() ? 0 : b.fields.size() * LINE_H + 2 * SECTION_PAD;
            double methodsH = b.methods.isEmpty() ? 0 : b.methods.size() * LINE_H + 2 * SECTION_PAD;
            b.h = b.headerH + b.fieldsH + methodsH;
            if (b.fields.isEmpty() && b.methods.isEmpty()) {
                b.h += 10;
            }
        }
    }

    // ---- layout helpers -----------------------------------------------------------------
    static Box b(String name) {
        Box box = boxes.get(name);
        if (box == null) {
            throw new IllegalArgumentException(name);
        }
        return box;
    }

    static void at(String name, double x, double y) {
        b(name).x = x;
        b(name).y = y;
    }

    static Point2D p(double x, double y) {
        return new Point2D.Double(x, y);
    }

    static Edge edge(EdgeType type, Point2D... pts) {
        Edge e = new Edge(type, new ArrayList<>(Arrays.asList(pts)), new ArrayList<>());
        edges.add(e);
        return e;
    }

    /** Adds a label; align -1 = right-aligned at x, 0 = centered, 1 = left-aligned at x. */
    static void label(Edge e, String text, double x, double y, int align) {
        e.labels().add(new Label(text, x, y, align));
    }

    // ---- rendering ----------------------------------------------------------------------
    static final Stroke SOLID = new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    static final Stroke DASHED = new BasicStroke(1.4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10f,
        new float[] {7f, 5f}, 0f);

    static void drawEdge(Graphics2D g, Edge e) {
        List<Point2D> pts = e.points();
        Point2D end = pts.get(pts.size() - 1);
        Point2D prev = pts.get(pts.size() - 2);
        double ang = Math.atan2(end.getY() - prev.getY(), end.getX() - prev.getX());
        boolean triangle = e.type() == EdgeType.GENERALIZATION || e.type() == EdgeType.REALIZATION;
        double headLen = triangle ? 14 : 11;

        // shorten the last segment so the line stops at the base of a triangle head
        Point2D lineEnd = triangle
            ? p(end.getX() - Math.cos(ang) * headLen, end.getY() - Math.sin(ang) * headLen)
            : end;

        Path2D path = new Path2D.Double();
        path.moveTo(pts.get(0).getX(), pts.get(0).getY());
        for (int i = 1; i < pts.size() - 1; i++) {
            path.lineTo(pts.get(i).getX(), pts.get(i).getY());
        }
        path.lineTo(lineEnd.getX(), lineEnd.getY());

        g.setColor(LINE);
        g.setStroke(e.type() == EdgeType.REALIZATION || e.type() == EdgeType.DEPENDENCY ? DASHED : SOLID);
        g.draw(path);

        g.setStroke(SOLID);
        double spread = triangle ? Math.toRadians(28) : Math.toRadians(26);
        Point2D a = p(end.getX() - Math.cos(ang - spread) * headLen / Math.cos(spread),
                      end.getY() - Math.sin(ang - spread) * headLen / Math.cos(spread));
        Point2D c = p(end.getX() - Math.cos(ang + spread) * headLen / Math.cos(spread),
                      end.getY() - Math.sin(ang + spread) * headLen / Math.cos(spread));
        if (triangle) {
            Path2D head = new Path2D.Double();
            head.moveTo(end.getX(), end.getY());
            head.lineTo(a.getX(), a.getY());
            head.lineTo(c.getX(), c.getY());
            head.closePath();
            g.setColor(BG);
            g.fill(head);
            g.setColor(LINE);
            g.draw(head);
        } else {
            g.draw(new Line2D.Double(end, a));
            g.draw(new Line2D.Double(end, c));
        }
    }

    static void drawLabel(Graphics2D g, Label l) {
        g.setFont(LABEL);
        FontMetrics fm = g.getFontMetrics();
        double w = fm.stringWidth(l.text());
        double x = l.align() < 0 ? l.x() - w : l.align() == 0 ? l.x() - w / 2 : l.x();
        double y = l.y();
        g.setColor(new Color(255, 255, 255, 230));
        g.fill(new RoundRectangle2D.Double(x - 3, y - fm.getAscent() + 1, w + 6, fm.getAscent() + 3, 4, 4));
        g.setColor(MUTED);
        g.drawString(l.text(), (float) x, (float) y);
    }

    static void drawBox(Graphics2D g, Box b) {
        Color accent = accent(b.kind);
        RoundRectangle2D outer = new RoundRectangle2D.Double(b.x, b.y, b.w, b.h, 10, 10);

        // soft shadow
        g.setColor(new Color(0, 0, 0, 18));
        g.fill(new RoundRectangle2D.Double(b.x + 2, b.y + 3, b.w, b.h, 10, 10));

        g.setColor(Color.WHITE);
        g.fill(outer);

        // header
        java.awt.Shape clip = g.getClip();
        g.clip(outer);
        g.setColor(headerFill(b.kind));
        g.fill(new Rectangle2D.Double(b.x, b.y, b.w, b.headerH));
        g.setColor(accent);
        g.fill(new Rectangle2D.Double(b.x, b.y, b.w, 4));
        g.setClip(clip);

        String st = stereotype(b.kind);
        double ty = b.y + 4;
        if (st != null) {
            g.setFont(STEREO);
            g.setColor(accent.darker());
            g.drawString(st, (float) (b.cx() - stereoFm.stringWidth(st) / 2.0), (float) (ty + 15));
            ty += 13;
        }
        g.setFont(b.kind == Kind.ABSTRACT || b.kind == Kind.INTERFACE ? NAME_ITALIC : NAME);
        FontMetrics fm = g.getFontMetrics();
        g.setColor(INK);
        g.drawString(b.name, (float) (b.cx() - fm.stringWidth(b.name) / 2.0), (float) (ty + 20));

        // separators
        g.setStroke(new BasicStroke(1f));
        g.setColor(new Color(0xD0D7DE));
        g.draw(new Line2D.Double(b.x, b.y + b.headerH, b.right(), b.y + b.headerH));
        double y = b.y + b.headerH;
        if (!b.fields.isEmpty() && !b.methods.isEmpty()) {
            g.draw(new Line2D.Double(b.x, y + b.fieldsH, b.right(), y + b.fieldsH));
        }

        g.setFont(MEMBER);
        y += SECTION_PAD;
        for (String m : b.fields) {
            drawMember(g, m, b.x + PAD_X, y + 12);
            y += LINE_H;
        }
        if (!b.fields.isEmpty()) {
            y += 2 * SECTION_PAD;
        }
        for (String m : b.methods) {
            drawMember(g, m, b.x + PAD_X, y + 12);
            y += LINE_H;
        }

        g.setStroke(new BasicStroke(1.3f));
        g.setColor(accent);
        g.draw(outer);
    }

    static void drawMember(Graphics2D g, String m, double x, double y) {
        boolean isStatic = m.startsWith("_");
        String text = plain(m);
        g.setColor(INK);
        if (text.startsWith("- ")) {
            g.setColor(MUTED);
        }
        g.drawString(text, (float) x, (float) y);
        if (isStatic) {
            double w = memberFm.stringWidth(text);
            g.setStroke(new BasicStroke(0.9f));
            g.draw(new Line2D.Double(x, y + 2, x + w, y + 2));
        }
    }

    static Rectangle2D packageBounds(String pkg) {
        Rectangle2D r = null;
        for (Box b : boxes.values()) {
            if (b.pkg.equals(pkg)) {
                Rectangle2D br = new Rectangle2D.Double(b.x, b.y, b.w, b.h);
                r = r == null ? br : r.createUnion(br);
            }
        }
        return r;
    }

    static final Map<String, double[]> PKG_EXTRA = new LinkedHashMap<>();

    static void drawPackage(Graphics2D g, String pkg) {
        Rectangle2D r = packageBounds(pkg);
        double[] extra = PKG_EXTRA.getOrDefault(pkg, new double[] {0, 0, 0, 0});
        double pad = 28;
        double x = r.getX() - pad - extra[0];
        double y = r.getY() - pad - 22 - extra[1];
        double w = r.getWidth() + 2 * pad + extra[0] + extra[2];
        double h = r.getHeight() + 2 * pad + 22 + extra[1] + extra[3];

        g.setColor(PKG_FILL.get(pkg));
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 18, 18));
        g.setColor(new Color(0xC9D1D9));
        g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10f, new float[] {6f, 4f}, 0f));
        g.draw(new RoundRectangle2D.Double(x, y, w, h, 18, 18));

        g.setFont(PKG);
        g.setColor(MUTED);
        g.drawString("com.taxify." + pkg, (float) (x + 16), (float) (y + 24));
    }

    public static void main(String[] args) throws Exception {
        BufferedImage probe = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D pg = probe.createGraphics();
        pg.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        memberFm = pg.getFontMetrics(MEMBER);
        nameFm = pg.getFontMetrics(NAME_ITALIC);
        stereoFm = pg.getFontMetrics(STEREO);

        defineModel();
        measure();

        if (args.length > 1 && args[1].equals("--sizes")) {
            for (Box b : boxes.values()) {
                System.out.printf("%-22s w=%4.0f h=%4.0f%n", b.name, b.w, b.h);
            }
            return;
        }

        double[] canvas = Layout.apply();
        double width = canvas[0];
        double height = canvas[1];
        double scale = 2;

        BufferedImage img = new BufferedImage((int) (width * scale), (int) (height * scale), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.scale(scale, scale);

        g.setColor(BG);
        g.fillRect(0, 0, (int) width, (int) height);

        g.setFont(TITLE);
        g.setColor(INK);
        g.drawString("Taxify — Class Diagram", 40, 52);
        g.setFont(SUBTITLE);
        g.setColor(MUTED);
        g.drawString("Ride-hailing and micro-mobility simulation in Java · Thomas & Eileen", 40, 76);

        for (String pkg : List.of("simulation", "company", "people", "vehicle", "map")) {
            drawPackage(g, pkg);
        }
        for (Edge e : edges) {
            drawEdge(g, e);
        }
        for (Box b : boxes.values()) {
            drawBox(g, b);
        }
        for (Edge e : edges) {
            for (Label l : e.labels()) {
                drawLabel(g, l);
            }
        }
        Legend.draw(g, width, height);

        g.dispose();
        ImageIO.write(img, "png", new File(args[0]));
        System.out.println("wrote " + args[0] + " " + img.getWidth() + "x" + img.getHeight());
    }
}

/** Hand-tuned positions and edge routes. */
class Layout {
    static ClassDiagram.Box b(String n) { return ClassDiagram.b(n); }
    static Point2D p(double x, double y) { return ClassDiagram.p(x, y); }
    static void at(String n, double x, double y) { ClassDiagram.at(n, x, y); }

    static final ClassDiagram.EdgeType GEN = ClassDiagram.EdgeType.GENERALIZATION;
    static final ClassDiagram.EdgeType REAL = ClassDiagram.EdgeType.REALIZATION;
    static final ClassDiagram.EdgeType ASSOC = ClassDiagram.EdgeType.ASSOCIATION;
    static final ClassDiagram.EdgeType DEP = ClassDiagram.EdgeType.DEPENDENCY;

    static ClassDiagram.Edge e(ClassDiagram.EdgeType t, Point2D... pts) { return ClassDiagram.edge(t, pts); }
    static void lbl(ClassDiagram.Edge e, String s, double x, double y, int a) { ClassDiagram.label(e, s, x, y, a); }

    /** Vertical edge from the top of child up to the bottom of parent at the given x. */
    static ClassDiagram.Edge up(ClassDiagram.EdgeType t, String child, String parent, double x) {
        return e(t, p(x, b(child).y), p(x, b(parent).bottom()));
    }

    /** Tree edge: child top-center up to a bus line, across, then up into the parent bottom-center. */
    static void tree(ClassDiagram.EdgeType t, String parent, String... children) {
        ClassDiagram.Box par = b(parent);
        for (String c : children) {
            ClassDiagram.Box ch = b(c);
            double busY = (par.bottom() + ch.y) / 2;
            e(t, p(ch.cx(), ch.y), p(ch.cx(), busY), p(par.cx(), busY), p(par.cx(), par.bottom()));
        }
    }

    static double[] apply() {
        final double G = 80;      // gap between boxes in a package
        final double BAND = 118;  // box-to-box distance across stacked packages

        // ---- band 1: simulation | people ------------------------------------------------
        double y1 = 170;
        at("Main", 70, y1);
        at("ApplicationSimulator", b("Main").right() + G + 10, y1);
        at("IApplicationSimulator", b("ApplicationSimulator").right() + G, y1);
        at("ApplicationLibrary", b("IApplicationSimulator").right() + G, y1);

        double px = b("ApplicationLibrary").right() + 28 + 60 + 28 + 40;
        at("IUser", px, y1);
        at("Person", b("IUser").right() + G, y1);
        at("IDriver", b("Person").right() + G, y1);
        double y1b = y1 + b("Person").h + G;
        at("User", (b("IUser").cx() + b("Person").cx()) / 2 - b("User").w / 2 + 30, y1b);
        at("Driver", (b("Person").cx() + b("IDriver").cx()) / 2 - b("Driver").w / 2 + 20, y1b);
        double band1Bottom = Math.max(b("User").bottom(), b("ApplicationLibrary").bottom());

        // ---- band 2: company | map ------------------------------------------------------
        double y2 = band1Bottom + BAND;
        at("ISubject", 70, y2);
        at("IObserver", b("ISubject").right() + G, y2);
        at("ITaxiCompany", b("IObserver").right() + G, y2);
        at("IService", b("ITaxiCompany").right() + G, y2);
        at("IStatistics", b("IService").right() + G + 80, y2);
        double row1H = 0;
        for (String n : new String[] {"ISubject", "IObserver", "ITaxiCompany", "IService", "IStatistics"}) {
            row1H = Math.max(row1H, b(n).h);
        }
        double y2b = y2 + row1H + G;
        at("TaxiCompany", b("ITaxiCompany").x, y2b);
        at("Service", b("IService").x, y2b);
        at("Statistics", b("IStatistics").right() - b("Statistics").w, y2b);
        at("RideMode", b("Service").x + 40, b("Service").bottom() + 70);

        double mx = b("IStatistics").right() + 28 + 60 + 28 + 30;
        at("Location", mx, 0);
        at("ILocation", 0, 0);
        b("ILocation").x = b("Location").cx() - b("ILocation").w / 2;
        b("ILocation").y = y2;
        b("Location").y = b("ILocation").bottom() + G;
        at("Route", b("Location").right() + G + 40, b("Location").y);
        b("IRoute").x = b("Route").cx() - b("IRoute").w / 2;
        b("IRoute").y = b("ILocation").y;

        double band2Bottom = Math.max(b("TaxiCompany").bottom(), b("RideMode").bottom());

        // ---- band 3: vehicle -------------------------------------------------------------
        double y3 = band2Bottom + BAND;
        // bottom row first so parents can center over children
        at("Taxi", 150, 0);
        at("Shuttle", b("Taxi").right() + 60, 0);
        at("Bike", b("Shuttle").right() + 110, 0);
        at("Scooter", b("Bike").right() + 60, 0);

        ClassDiagram.Box vehicle = b("Vehicle");
        vehicle.x = (b("Taxi").cx() + b("Shuttle").cx()) / 2 - vehicle.w / 2;
        ClassDiagram.Box micro = b("MicroVehicle");
        micro.x = (b("Bike").cx() + b("Scooter").cx()) / 2 - micro.w / 2;
        ClassDiagram.Box iv = b("IVehicle");
        iv.x = (vehicle.cx() + micro.cx()) / 2 - iv.w / 2;
        iv.y = y3;
        at("IMovable", iv.x - G - b("IMovable").w - 40, y3 + 40);

        double y3b = iv.bottom() + G;
        vehicle.y = y3b;
        micro.y = y3b;
        at("VehicleStatus", vehicle.x - G - b("VehicleStatus").w + 10, y3b + 50);
        at("MicroVehicleStatus", micro.right() + G - 10, y3b + 50);

        double y3c = Math.max(vehicle.bottom(), micro.bottom()) + G;
        for (String n : new String[] {"Taxi", "Shuttle", "Bike", "Scooter"}) {
            b(n).y = y3c;
        }
        at("RentalVehicleType", b("Scooter").right() + G, y3c + 40);

        ClassDiagram.PKG_EXTRA.put("vehicle", new double[] {0, 0, 0, 40});

        // ---- edges -----------------------------------------------------------------------
        ClassDiagram.Edge ed;

        // simulation
        ClassDiagram.Box main = b("Main");
        ClassDiagram.Box sim = b("ApplicationSimulator");
        double my = main.y + 52;
        ed = e(DEP, p(main.right(), my), p(sim.x, my));
        lbl(ed, "«creates»", (main.right() + sim.x) / 2, my - 7, 0);
        double ry = sim.y + 70;
        e(REAL, p(sim.right(), ry), p(b("IApplicationSimulator").x, ry));

        // simulation -> company
        double obsX = Math.max(sim.x, b("IObserver").x) + 50;
        e(REAL, p(obsX, sim.bottom()), p(obsX, b("IObserver").y));
        double tcX = Math.min(sim.right(), b("ITaxiCompany").right()) - 30;
        ed = e(ASSOC, p(tcX, sim.bottom()), p(tcX, b("ITaxiCompany").y));
        lbl(ed, "company  1", tcX + 8, b("ITaxiCompany").y - 10, 1);

        // people
        up(REAL, "User", "IUser", Math.min(b("User").x + 50, b("IUser").right() - 40));
        up(GEN, "User", "Person", Math.max(b("User").right() - 50, b("Person").x + 40));
        up(GEN, "Driver", "Person", Math.min(b("Driver").x + 50, b("Person").right() - 40));
        up(REAL, "Driver", "IDriver", Math.max(b("Driver").right() - 60, b("IDriver").x + 40));

        // company
        ClassDiagram.Box tc = b("TaxiCompany");
        up(REAL, "TaxiCompany", "ITaxiCompany", tc.cx());
        double obsLineY = tc.y + 40;
        ed = e(ASSOC, p(tc.x, obsLineY), p(b("IObserver").cx(), obsLineY), p(b("IObserver").cx(), b("IObserver").bottom()));
        lbl(ed, "observer  0..1", b("IObserver").cx() + 8, b("IObserver").bottom() + 22, 1);
        double subLineY = tc.y + 110;
        e(REAL, p(tc.x, subLineY), p(b("ISubject").cx(), subLineY), p(b("ISubject").cx(), b("ISubject").bottom()));
        up(REAL, "Service", "IService", b("IService").cx());
        up(REAL, "Statistics", "IStatistics", b("Statistics").cx());
        ClassDiagram.Box rm = b("RideMode");
        ed = e(DEP, p(rm.cx(), b("Service").bottom()), p(rm.cx(), rm.y));
        lbl(ed, "rideMode", rm.cx() + 8, (b("Service").bottom() + rm.y) / 2 + 4, 1);

        // company -> people (through the channel between band 1 and band 2)
        double chanHi = band1Bottom + 28 + 12;
        double chanLo = chanHi + 18;
        ClassDiagram.Box iuser = b("IUser");
        double gapX1 = (b("ITaxiCompany").right() + b("IService").x) / 2 - 18;
        double land1 = iuser.x + 60;
        ed = e(ASSOC, p(tc.right(), tc.y + 30), p(gapX1, tc.y + 30), p(gapX1, chanHi), p(land1, chanHi), p(land1, iuser.bottom()));
        lbl(ed, "users  *", land1 - 8, iuser.bottom() + 22, -1);
        ClassDiagram.Box svc = b("Service");
        double gapX2 = (b("IService").right() + b("IStatistics").x) / 2;
        double land2 = iuser.x + 130;
        ed = e(ASSOC, p(gapX2, svc.y), p(gapX2, chanLo), p(land2, chanLo), p(land2, iuser.bottom()));
        lbl(ed, "users  1..2", land2 + 8, iuser.bottom() + 22, 1);

        // map
        up(REAL, "Location", "ILocation", b("ILocation").cx());
        up(REAL, "Route", "IRoute", b("IRoute").cx());
        ClassDiagram.Box route = b("Route");
        ClassDiagram.Box iloc = b("ILocation");
        double rx = route.x + 40;
        ed = e(ASSOC, p(rx, route.y), p(rx, iloc.cy()), p(iloc.right(), iloc.cy()));
        lbl(ed, "route  *", iloc.right() + 10, iloc.cy() - 8, 1);

        // company -> vehicle
        double tvX = Math.max(tc.x, iv.x) + 70;
        ed = e(ASSOC, p(tvX, tc.bottom()), p(tvX, iv.y));
        lbl(ed, "vehicles  *", tvX + 8, iv.y - 10, 1);
        double svcX = (b("ITaxiCompany").right() + b("IService").x) / 2 + 18;
        double svcY = iv.y + 40;
        ed = e(ASSOC, p(iv.right(), svcY), p(svcX, svcY), p(svcX, b("IService").y + 120), p(b("IService").x, b("IService").y + 120));
        lbl(ed, "service  0..1", iv.right() + 10, svcY - 8, 1);
        ClassDiagram.Box ist = b("IStatistics");
        double stX = (svc.right() + b("Statistics").x) / 2;
        double stY = iv.y + 90;
        ed = e(ASSOC, p(iv.right(), stY), p(stX, stY), p(stX, ist.bottom()));
        lbl(ed, "statistics  1", iv.right() + 10, stY - 8, 1);

        // vehicle
        ClassDiagram.Box mov = b("IMovable");
        e(GEN, p(iv.x, mov.cy()), p(mov.right(), mov.cy()));
        tree(REAL, "IVehicle", "Vehicle", "MicroVehicle");
        tree(GEN, "Vehicle", "Taxi", "Shuttle");
        tree(GEN, "MicroVehicle", "Bike", "Scooter");
        ClassDiagram.Box vs = b("VehicleStatus");
        ed = e(DEP, p(vehicle.x, vs.cy()), p(vs.right(), vs.cy()));
        lbl(ed, "status", (vehicle.x + vs.right()) / 2, vs.cy() - 8, 0);
        ClassDiagram.Box mvs = b("MicroVehicleStatus");
        ed = e(DEP, p(micro.right(), mvs.cy()), p(mvs.x, mvs.cy()));
        lbl(ed, "status", (micro.right() + mvs.x) / 2, mvs.cy() - 8, 0);
        ClassDiagram.Box rvt = b("RentalVehicleType");
        ClassDiagram.Box sc = b("Scooter");
        ClassDiagram.Box bike = b("Bike");
        ed = e(DEP, p(sc.right(), rvt.cy()), p(rvt.x, rvt.cy()));
        lbl(ed, "rentalType", (sc.right() + rvt.x) / 2, rvt.cy() - 8, 0);
        double under = Math.max(sc.bottom(), bike.bottom()) + 30;
        e(DEP, p(bike.cx(), bike.bottom()), p(bike.cx(), under), p(rvt.cx(), under), p(rvt.cx(), rvt.bottom()));

        // canvas
        double maxX = 0;
        double maxY = 0;
        for (ClassDiagram.Box bx : ClassDiagram.boxes.values()) {
            maxX = Math.max(maxX, bx.right());
            maxY = Math.max(maxY, bx.bottom());
        }
        return new double[] {maxX + 70, maxY + 110};
    }
}

/** Legend for box colours and line styles. */
class Legend {
    static void draw(Graphics2D g, double width, double height) {
        double x = ClassDiagram.b("Location").x;
        double y = ClassDiagram.b("Vehicle").y;
        double w = 420;
        double h = 330;

        g.setColor(new Color(0xF7F8FA));
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 16, 16));
        g.setColor(new Color(0xC9D1D9));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new RoundRectangle2D.Double(x, y, w, h, 16, 16));

        g.setFont(ClassDiagram.PKG);
        g.setColor(ClassDiagram.MUTED);
        g.drawString("Legend", (float) (x + 20), (float) (y + 30));

        String[] names = {"Interface", "Abstract class", "Class", "Enumeration"};
        ClassDiagram.Kind[] kinds = {ClassDiagram.Kind.INTERFACE, ClassDiagram.Kind.ABSTRACT,
            ClassDiagram.Kind.CLASS, ClassDiagram.Kind.ENUM};
        double ly = y + 52;
        for (int i = 0; i < names.length; i++) {
            double sx = x + 20;
            g.setColor(ClassDiagram.headerFill(kinds[i]));
            g.fill(new RoundRectangle2D.Double(sx, ly, 34, 20, 6, 6));
            g.setColor(ClassDiagram.accent(kinds[i]));
            g.draw(new RoundRectangle2D.Double(sx, ly, 34, 20, 6, 6));
            g.setFont(ClassDiagram.LABEL);
            g.setColor(ClassDiagram.INK);
            g.drawString(names[i], (float) (sx + 46), (float) (ly + 15));
            ly += 30;
        }

        String[] edgeNames = {"extends (generalization)", "implements (realization)",
            "association  (role  multiplicity)", "uses (dependency)"};
        ClassDiagram.EdgeType[] types = ClassDiagram.EdgeType.values();
        ly += 8;
        for (int i = 0; i < types.length; i++) {
            double sx = x + 20;
            ClassDiagram.drawEdge(g, new ClassDiagram.Edge(types[i],
                List.of(ClassDiagram.p(sx, ly + 10), ClassDiagram.p(sx + 70, ly + 10)), List.of()));
            g.setFont(ClassDiagram.LABEL);
            g.setColor(ClassDiagram.INK);
            g.drawString(edgeNames[i], (float) (sx + 86), (float) (ly + 15));
            ly += 30;
        }
        g.setFont(ClassDiagram.LABEL);
        g.setColor(ClassDiagram.MUTED);
        g.drawString("+ public   - private   underlined = static", (float) (x + 20), (float) (ly + 14));
    }
}
