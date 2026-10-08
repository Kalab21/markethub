"""Generate the MarketHub architecture diagram (light + dark SVG).

Standard library only. Run from the repository root:

    python docs/diagrams/generate_diagrams.py

Writes docs/markethub-architecture.svg and docs/markethub-architecture-dark.svg.
"""
import math
from html import escape
from pathlib import Path

OUT = Path(__file__).resolve().parent.parent

THEMES = {
    "light": dict(bg="#ffffff", box="#f3f5fb", box2="#fafbff", group="#fafbff", group_stroke="#9aa3d9",
                  stroke="#3f4ab8", title="#14213d", text="#4b587c", accent="#3f4ab8",
                  amber="#9a6700", amber_fill="#fff8e6", data_fill="#eef6fb", data="#0b6f8a"),
    "dark": dict(bg="#0d1117", box="#161b22", box2="#11161d", group="#11161d", group_stroke="#4a5280",
                 stroke="#8b93e8", title="#e6edf3", text="#9aa4b2", accent="#8b93e8",
                 amber="#e3b341", amber_fill="#2b2410", data_fill="#0f2430", data="#4fb3cf"),
}

ALT = ("MarketHub architecture: a browser used by Admin, Seller and Buyer sends web requests into one "
       "Spring Boot application, through the Spring Security filter chain (form login, session, roles, CSRF), "
       "Spring MVC controllers (server-rendered Thymeleaf + Bootstrap pages and /api/** REST endpoints), "
       "the @Transactional service layer and Spring Data JPA repositories, to MySQL 8 over JDBC.")


class Svg:
    def __init__(self, w, h, t, label):
        self.w, self.h, self.t, self.parts, self.label = w, h, t, [], label

    def rect(self, x, y, w, h, fill, stroke, dash=False, rx=12, sw=2):
        d = ' stroke-dasharray="7 6"' if dash else ""
        self.parts.append(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{rx}" fill="{fill}" '
                          f'stroke="{stroke}" stroke-width="{sw}"{d}/>')

    def text(self, x, y, s, size=15, weight=400, color=None, anchor="middle"):
        self.parts.append(f'<text x="{x}" y="{y}" font-size="{size}" font-weight="{weight}" '
                          f'fill="{color or self.t["text"]}" text-anchor="{anchor}">{escape(s)}</text>')

    def box(self, x, y, w, h, title, lines=(), title_color=None, fill=None, stroke=None):
        self.rect(x, y, w, h, fill or self.t["box"], stroke or self.t["stroke"])
        n = 1 + len(lines)
        top = y + h / 2 - (n - 1) * 11 + 6
        self.text(x + w / 2, top, title, 18, 700, title_color or self.t["title"])
        for i, ln in enumerate(lines):
            self.text(x + w / 2, top + 24 + i * 21, ln, 14.5)

    def arrow(self, pts, color=None, label=None, lx=None, ly=None):
        # Explicit triangle arrowheads (SVG markers are not rendered by every viewer).
        c = color or self.t["accent"]
        (x1, y1), (x2, y2) = pts[-2], pts[-1]
        a = math.atan2(y2 - y1, x2 - x1)
        hl, hw = 13, 7
        bx, by = x2 - hl * math.cos(a), y2 - hl * math.sin(a)
        line = list(pts[:-1]) + [(bx, by)]
        path = " ".join(f"{'M' if i == 0 else 'L'}{x:.1f},{y:.1f}" for i, (x, y) in enumerate(line))
        self.parts.append(f'<path d="{path}" fill="none" stroke="{c}" stroke-width="2.5"/>')
        p1 = (bx + hw * math.sin(a), by - hw * math.cos(a))
        p2 = (bx - hw * math.sin(a), by + hw * math.cos(a))
        self.parts.append(f'<path d="M{x2:.1f},{y2:.1f} L{p1[0]:.1f},{p1[1]:.1f} '
                          f'L{p2[0]:.1f},{p2[1]:.1f} z" fill="{c}"/>')
        if label:
            self.text(lx, ly, label, 13.5, 600, c, "start")

    def render(self):
        return (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {self.w} {self.h}" width="{self.w}" '
                f'height="{self.h}" role="img" aria-label="{escape(self.label)}">\n'
                '<style>text{font-family:-apple-system,"Segoe UI",Roboto,Helvetica,Arial,sans-serif}</style>\n'
                f'<rect width="{self.w}" height="{self.h}" fill="{self.t["bg"]}"/>\n'
                + "\n".join(self.parts) + "\n</svg>\n")


def markethub(t):
    W, H = 1000, 950
    s = Svg(W, H, t, ALT)
    cx = W / 2
    bx, bw = 230, 540

    # Client zone (outside the application boundary)
    s.text(60, 40, "Client", 14.5, 700, t["text"], "start")
    s.box(bx, 30, bw, 88, "Browser", ["Admin  ·  Seller  ·  Buyer"], fill=t["box2"])

    # Application boundary: one Spring Boot deployable
    gy, gh = 170, 560
    s.rect(60, gy, 880, gh, t["group"], t["group_stroke"], dash=True, rx=18)
    s.text(84, gy + 30, "Application boundary  ·  one Spring Boot application (Java 17)", 15, 700, t["accent"], "start")

    layers = [
        ("Spring Security filter chain", ["form login · server-side session · roles · CSRF"],
         t["amber"], t["amber_fill"], t["amber"], 92),
        ("Spring MVC controllers", ["server-rendered UI: Thymeleaf + Bootstrap 5.3",
                                     "/api/** REST endpoints"], None, None, None, 104),
        ("Service layer", ["business rules · @Transactional"], None, None, None, 92),
        ("Spring Data JPA repositories", ["Hibernate entities and queries"], None, None, None, 92),
    ]
    y = gy + 54
    prev = 30 + 88
    for i, (title, lines, tc, fill, stroke, h) in enumerate(layers):
        if i == 0:
            s.arrow([(cx, prev), (cx, y)], label="Web request", lx=cx + 14, ly=(prev + gy) / 2 + 5)
        else:
            s.arrow([(cx, prev), (cx, y)])
        s.box(bx, y, bw, h, title, lines, title_color=tc, fill=fill, stroke=stroke)
        prev = y + h
        y += h + 32

    # Data zone
    dy = gy + gh + 50
    s.rect(60, dy, 880, 140, t["data_fill"], t["data"], rx=18)
    s.text(84, dy + 30, "Data", 15, 700, t["data"], "start")
    s.arrow([(cx, prev), (cx, dy + 34)], color=t["data"], label="JDBC", lx=cx + 14, ly=(prev + dy) / 2 + 5)
    s.box(bx, dy + 34, bw, 86, "MySQL 8", ["H2 in tests  ·  Docker Compose locally"],
          title_color=t["data"], fill=t["bg"], stroke=t["data"])
    return s.render()


if __name__ == "__main__":
    for theme, palette in THEMES.items():
        suffix = "" if theme == "light" else "-dark"
        (OUT / f"markethub-architecture{suffix}.svg").write_text(markethub(palette), encoding="utf8")
    print("written")
