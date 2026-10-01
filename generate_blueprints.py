from PIL import Image, ImageDraw, ImageFont, ImageFilter
from pathlib import Path
import json, math, os, zipfile, textwrap, shutil, glob

# Destination directories
OUT = Path(r"C:\Users\MrJws\OneDrive\Workspaces\Thunder Dome\stage_blueprints")
OUT.mkdir(parents=True, exist_ok=True)

ARTIFACT_OUT = Path(r"C:\Users\MrJws\.gemini\antigravity\brain\15baa7c6-3701-4d45-8501-acc79ffd3170\stage_blueprints")
ARTIFACT_OUT.mkdir(parents=True, exist_ok=True)

USER_UPLOADED_DIR = Path(r"C:\Users\MrJws\.gemini\antigravity\brain\15baa7c6-3701-4d45-8501-acc79ffd3170\.user_uploaded")

# Official 3000 Studios 6-Panel Storyboard Reference Sheets
sheets = {
    (1,6): USER_UPLOADED_DIR / "media_1790893492205.jpg",   # Cinematic Cutscenes Stages 01-06
    (7,12): USER_UPLOADED_DIR / "media_1790893492167.jpg",  # Cinematic Cutscenes Stages 07-12
    (13,18): USER_UPLOADED_DIR / "media_1790893492188.jpg", # Cinematic Cutscenes Stages 13-18
    (19,24): USER_UPLOADED_DIR / "media_1790893533398.jpg", # Cinematic Cutscenes Stages 19-24
}

# Fonts
def load_font(size, bold=False):
    candidates = [
        "C:/Windows/Fonts/segoeuib.ttf" if bold else "C:/Windows/Fonts/segoeui.ttf",
        "C:/Windows/Fonts/arialbd.ttf" if bold else "C:/Windows/Fonts/arial.ttf",
        "C:/Windows/Fonts/tahomabd.ttf" if bold else "C:/Windows/Fonts/tahoma.ttf",
        "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf" if bold else "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
        "/usr/share/fonts/truetype/liberation2/LiberationSans-Bold.ttf" if bold else "/usr/share/fonts/truetype/liberation2/LiberationSans-Regular.ttf",
    ]
    for p in candidates:
        if os.path.exists(p):
            try:
                return ImageFont.truetype(p, size=size)
            except Exception:
                pass
    return ImageFont.load_default()

F_TITLE = load_font(70, True)
F_H1 = load_font(38, True)
F_H2 = load_font(28, True)
F_BODY = load_font(24, False)
F_SMALL = load_font(20, False)
F_TINY = load_font(17, False)
F_TAG = load_font(22, True)

stages = [
    dict(n=1, name="NEON OUTPOST", weather="NEON RAIN", boss="SHADOW WRAITH", boss_quote="You can't see me.", boss_class="STEALTH CLASS",
         palette=("#08121E","#00D9FF","#FF2C9C","#2478FF","#7B2CFF"),
         obstacles=["AA turret nests","holo-billboard canyon","laser-road gates","wet rooftop pylons","drone traffic"],
         speed=["40% cyan booster lane","76% rail-sling war-speed strip"],
         boss_abilities=["Stealth Field (3s Invisible)","Shadow Missiles (Homing Swarm)","Crossfire Laser Lattice"],
         hero="Black chrome + cyan edge light + magenta circuit filigree; reflective wet-look clearcoat."),
    dict(n=2, name="ASTEROID BELT", weather="SPACE DUST", boss="VOLT SPIKE", boss_quote="Feel the current.", boss_class="CHAIN CLASS",
         palette=("#120B08","#D65A0A","#FF7A00","#6E2AD8","#2A221F"),
         obstacles=["rotating asteroid clusters","magnetic purple mines","cratered rock arches","debris fields","tumbling boulders"],
         speed=["38% debris slingshot","72% twin-asteroid gravity boost"],
         boss_abilities=["Chain Lightning (Bounces Targets)","EMP Burst (Disables Speed)","Broadside Flak Cannon"],
         hero="Gunmetal hull + amber hazard stripes + violet anti-grav cores; chipped rock-scar decals."),
    dict(n=3, name="VOID GATE", weather="VOID WARP", boss="CRIMSON FANG", boss_quote="Three shots, your end.", boss_class="BURST CLASS",
         palette=("#09020F","#8A2BE2","#FF21D6","#5D35B8","#050505"),
         obstacles=["gravity shear rings","void spike corridors","warp-orb mines","fractured obsidian slabs","portal turbulence"],
         speed=["34% portal sling","74% vortex acceleration tunnel"],
         boss_abilities=["Rapid Fire (Triple Shot)","Blood Orb (Life Drain On Hit)","Teleport Ambush"],
         hero="Obsidian ceramic + violet plasma veins + magenta portal glyphs; starfield panel texture."),
    dict(n=4, name="TOXIC SECTOR", weather="TOXIC MIST", boss="TOXIN HAZE", boss_quote="Breathe deeper.", boss_class="HAZARD CLASS",
         palette=("#061006","#42FF30","#92D811","#D8EA23","#121212"),
         obstacles=["caustic gas clouds","acid puddle vents","corroded pipe towers","sludge channels","biohazard fans"],
         speed=["42% pressure-vent thrust lane","78% reactor exhaust warp strip"],
         boss_abilities=["Gas Cloud (Area Damage)","Corrosion Beam (Armor Reduce)","Toxic Spore Swarm"],
         hero="Matte black + luminous toxic green vents + yellow warning chevrons; biohazard stencil skin."),
    dict(n=5, name="ICE FORTRESS", weather="BLIZZARD", boss="FROST NOVA", boss_quote="Everything freezes.", boss_class="CONTROL CLASS",
         palette=("#061420","#56C8FF","#F2FAFF","#5B8FCC","#1F5A99"),
         obstacles=["ice spike walls","frost turrets","glacier crevasses","wind shear zones","frozen bridge arches"],
         speed=["36% ice-canyon slipstream","71% frozen launch rail"],
         boss_abilities=["Freeze Pulse (Slows Player)","Ice Shards (Spread Shot)","Crystal Armor Rebuild"],
         hero="Brushed steel + ice-blue emissive ribs + white frost fade; faceted crystal wing tips."),
    dict(n=6, name="SOLAR CORE", weather="SOLAR FLARES", boss="SOLAR FLARE", boss_quote="Burn brighter.", boss_class="AREA CLASS",
         palette=("#1A0900","#FFB000","#FF6A00","#FF2A1A","#4A1200"),
         obstacles=["solar flare curtains","plasma jets","heat-plate rings","burning trails","corona shockwaves"],
         speed=["41% plasma draft","77% corona slingshot"],
         boss_abilities=["Burning Trail (Damage Over Time)","Solar Beam (Piercing Shot)","Corona Overload Pulse"],
         hero="Mirror black + molten gold trim + orange heat vents; sunburst wing graphics with ember clearcoat."),
    dict(n=7, name="CYBER CITY", weather="NEON RAIN", boss="VOID REAPER", boss_quote="All things end here.", boss_class="GRAVITY CLASS",
         palette=("#070916","#FF22CB","#00CFFF","#3B45FF","#7E2FB3"),
         obstacles=["skyscraper canyon","traffic drones","holo-ad minefields","service bridges","electric rooftop fences"],
         speed=["39% maglev corridor","73% neon transit warp lane"],
         boss_abilities=["Black Hole (Pulls Player)","Void Orbs (Explode On Contact)","Shadow Dash Ram"],
         hero="Carbon fiber + cyan/magenta racing graphics + animated equalizer strips along the fuselage."),
    dict(n=8, name="JUNKYARD", weather="RUST STORM", boss="BLADE STORM", boss_quote="Spin until you break.", boss_class="SPIN CLASS",
         palette=("#130D08","#F36B17","#8C3D1F","#64412B","#181818"),
         obstacles=["sawblade debris","capital hull wrecks","scrap ambushes","explosive piles","crane arms"],
         speed=["35% salvage-catapult lane","70% turbine-corridor boost"],
         boss_abilities=["Spinning Blades (Circular Attack)","Ricochet Shot (Bounces)","Scrap Cyclone Blast"],
         hero="Weathered titanium + orange weld seams + stenciled serial numbers; patchwork armored panels."),
    dict(n=9, name="BLACK HOLE", weather="GRAVITY PULL", boss="NEON PHANTOM", boss_quote="I am everywhere.", boss_class="PHASE CLASS",
         palette=("#05030A","#6E35E7","#A647FF","#341D78","#000000"),
         obstacles=["singularity pull zones","lensing rings","distorted asteroids","event-horizon lanes","tidal debris"],
         speed=["43% gravity-assist arc","79% horizon-surf warp burst"],
         boss_abilities=["Phase Dash (Fast Movement)","Neon Lasers (Wide Spread)","Afterimage Swarm"],
         hero="Ultra-black hull + purple lensing rings + violet star specks; curved gravitational distortion graphics."),
    dict(n=10, name="LAVA PLANET", weather="LAVA RAIN", boss="OMEGA DRONE", boss_quote="Many become one.", boss_class="SWARM CLASS",
         palette=("#130500","#FF5218","#E62717","#3A3635","#050505"),
         obstacles=["magma bombs","lava eruptions","basalt spires","factory platforms","heat distortion pockets"],
         speed=["37% magma updraft lane","74% furnace-jet warp strip"],
         boss_abilities=["Summon Drones (3 Mini Ships)","Laser Grid (Cross Pattern)","Molten Missile Spread"],
         hero="Charcoal armor + red-hot cracks + orange underside glow; volcanic fracture graphic across wings."),
    dict(n=11, name="ORBITAL ARRAY", weather="ION DUST", boss="LIQUID METAL", boss_quote="Adapt, consume, repeat.", boss_class="MORPH CLASS",
         palette=("#05111B","#21A7F3","#9FD9FF","#1B64D4","#DCE8F1"),
         obstacles=["laser sweep rings","ion pulse nodes","station modules","satellite spokes","antenna fields"],
         speed=["40% ion conduit","75% ring-orbit catapult"],
         boss_abilities=["Morph Form (Shape Shift)","Metal Wave (Reflects Player Shot)","Reflective Quicksilver Armor"],
         hero="Polished silver + electric-blue circuitry + white ion streaks; satellite-ring insignia."),
    dict(n=12, name="SAND WASTES", weather="SAND STORM", boss="SAND VIPER", boss_quote="The storm obliterates.", boss_class="STORM CLASS",
         palette=("#1A120B","#D58A2B","#A25D27","#6D4527","#E9B64A"),
         obstacles=["sand tornadoes","EM dust bursts","canyon spires","buried ruins","rock arches"],
         speed=["33% dune crest tailwind","69% canyon vent war-speed"],
         boss_abilities=["Sand Tornado (Area Pull)","Razor Darts (High Speed)","Burrow Strike Ambush"],
         hero="Desert tan + black belly + gold edge guards; viper-scale wing graphics and dust-worn nose."),
    dict(n=13, name="BIO LABS", weather="BIO SPORES", boss="CELESTIAL GUARD", boss_quote="Judgment from above.", boss_class="DIVINE CLASS",
         palette=("#071008","#6FE51C","#A8E72B","#1F6D38","#D8E74A"),
         obstacles=["mutagen domes","glass tube towers","spore clouds","bio-weapon pods","slime channels"],
         speed=["41% nutrient-flow booster","77% gene-tube acceleration rail"],
         boss_abilities=["Divine Shield (Deflects Shots)","Holy Pulse (Stun)","Bio-Weapon Colossus Beam"],
         hero="Gloss black + luminous green vein lattice + translucent bio-cells; gene-helix wing markings."),
    dict(n=14, name="UNDERWATER RUINS", weather="HYDRO STREAM", boss="RADIATION CORE", boss_quote="Contamination spreads.", boss_class="MUTATE CLASS",
         palette=("#051620","#0DAED0","#53E7FF","#1B627C","#87F0ED"),
         obstacles=["sunken towers","hydro current lanes","bubble mines","caustic pillars","collapsed arches"],
         speed=["36% current jet","72% hydro-tunnel slingshot"],
         boss_abilities=["Radiation Burst (Area Damage)","Mutate Beacons (Slower Below 50% HP)","Abyssal Torpedo Spiral"],
         hero="Deep navy + cyan caustic shimmer + pearl-white trim; scale-like hydrodynamic pattern."),
    dict(n=15, name="SKY TEMPLE", weather="AURA BREEZE", boss="AQUA STRIKE", boss_quote="The depths claim you.", boss_class="WATER CLASS",
         palette=("#10233A","#8CCAF0","#F7FBFF","#7EA6C9","#D2EEF8"),
         obstacles=["floating island gaps","crystal shards","wind columns","temple gates","god-ray blind zones"],
         speed=["39% jetstream lane","75% celestial launch beam"],
         boss_abilities=["Water Cannon (Knockback)","Bubble Shield (Temporary Invincibility)","Wing-Lance Holy Rain"],
         hero="Pearl white + sky-blue inlays + gold micro-trim; feathered geometric graphics on wings."),
    dict(n=16, name="MACHINE WORLD", weather="SMELTER ASH", boss="MAGMA BRUTE", boss_quote="The planet bleeds.", boss_class="SIEGE CLASS",
         palette=("#120A07","#C94B18","#FF7A22","#633020","#181818"),
         obstacles=["crusher presses","moving belts","gear walls","molten drains","robotic foundry arms"],
         speed=["42% conveyor overdrive","78% smelter exhaust warp"],
         boss_abilities=["Magma Balls (Explosive Spread)","Lava Trail (Damage On Touch)","Hydraulic Smelter Shockwave"],
         hero="Blackened steel + copper welds + hot orange mechanical glyphs; gear-tooth wing striping."),
    dict(n=17, name="CRYSTAL CAVERNS", weather="CRYSTAL DUST", boss="CYBER HAWK", boss_quote="Target locked.", boss_class="HUNTER CLASS",
         palette=("#0B0715","#7A2AE8","#B44DFF","#34C9FF","#E0EAFF"),
         obstacles=["mirror crystal fields","laser reflections","shard avalanches","prism gates","fracture pits"],
         speed=["38% prism-refraction boost","73% crystal resonance warp"],
         boss_abilities=["Target Lock (Tracks Player)","Missile Swarm (5 Homing)","Reflective Feather Shield"],
         hero="Dark violet + iridescent crystal facets + cyan laser lines; holographic prismatic wing skin."),
    dict(n=18, name="STORM FRONT", weather="LIGHTNING", boss="QUANTUM SHIFT", boss_quote="Reality bends.", boss_class="TIME CLASS",
         palette=("#090C1A","#6947D9","#A157FF","#48B7FF","#D6E7FF"),
         obstacles=["lightning curtains","EMP arcs","storm vortices","charged cloud walls","temporal turbulence"],
         speed=["40% thunderhead updraft","76% lightning-rail war-speed"],
         boss_abilities=["Time Warp (Slows Time)","Quantum Blades (Teleporting Attack)","Temporal Shock Curtain"],
         hero="Midnight blue + white lightning forks + violet quantum rings; animated pulse texture on tail."),
    dict(n=19, name="ALIEN JUNGLE", weather="GREEN-YELLOW LIGHTNING", boss="STORM LORD", boss_quote="Nature strikes back.", boss_class="NATURE CLASS",
         palette=("#06100C","#57D71D","#B8EA24","#1B6D54","#6639A5"),
         obstacles=["predatory vines","bioluminescent canopy","spore pods","living root gates","acid flower turrets"],
         speed=["37% canopy wind tunnel","72% bio-electric surge lane"],
         boss_abilities=["Thunder Strike (Random Lightning)","Storm Field (Constant Damage)","Charged Wing Dive"],
         hero="Forest-black + acid-green edge veins + purple bio-lights; alien leaf/fractal graphics."),
    dict(n=20, name="SPACE GRAVEYARD", weather="COLD SPECTRAL GLOW", boss="CRYSTAL REVENANT", boss_quote="The dead still fly.", boss_class="REFLECT CLASS",
         palette=("#050912","#2860D9","#443CB4","#7569E7","#292151"),
         obstacles=["wrecked battleship hulls","debris collisions","sniper corridors","engine carcasses","floating armor plates"],
         speed=["34% reactor-remnant boost","69% wreck-corridor gravity sling"],
         boss_abilities=["Crystal Spikes (Multi-Direction)","Reflective Armor (Bounces Shots)","Hull-Fragment Shield"],
         hero="Cold gunmetal + spectral blue-violet exhaust + ghosted fleet emblems; battle-scar skin."),
    dict(n=21, name="DIMENSION RIFT", weather="PURPLE-VIOLET GLITCHES", boss="INFERNO RIDER", boss_quote="Ride the flames.", boss_class="SPEED CLASS",
         palette=("#0B0414","#7720E8","#A42DDC","#E1297A","#B81731"),
         obstacles=["reality seams","phase walls","fractured chunks","glitch corridors","lava-wake scars"],
         speed=["41% phase skip","77% multiverse tear warp"],
         boss_abilities=["Fire Dash (Charges At Player)","Inferno Wave (Wide Flame)","Reality Phase Inversion"],
         hero="Black-violet base + magenta/red glitch slices + fractured mirror panels; chromatic split graphics."),
    dict(n=22, name="THE CITADEL", weather="RED WAR HAZE", boss="BIOSYNTH", boss_quote="Evolution continues.", boss_class="REGEN CLASS",
         palette=("#110708","#982123","#D73722","#6F202A","#262626"),
         obstacles=["heavy flak walls","command spires","defense rings","missile towers","armored blast doors"],
         speed=["39% launch-bay catapult","74% reactor trench overdrive"],
         boss_abilities=["Bio Missiles (Split On Hit)","Heal Over Time (Regenerate)","Command Defense Ring"],
         hero="Dark armor + crimson command stripes + metallic silver insignia; angular military geometry."),
    dict(n=23, name="FINAL APPROACH", weather="RED WAR HAZE", boss="GRAVITY TITAN", boss_quote="You belong to me.", boss_class="GRAVITY CLASS",
         palette=("#140707","#7E151B","#D01B12","#F04A18","#5A2B1D"),
         obstacles=["flagship armada lanes","missile walls","fighter swarms","capital cannon beams","burning wreck trails"],
         speed=["42% carrier launch wake","80% final assault war-speed corridor"],
         boss_abilities=["Gravity Field (Slows Movement)","Meteor Drop (Falling Rocks)","Final Fleet Armada Beam"],
         hero="Satin black + deep red spear graphics + orange afterburner blades; campaign kill-mark decals."),
    dict(n=24, name="THUNDER DOME", weather="COLOSSEUM LIGHTNING", boss="NEXUS OBLITERATOR", boss_quote="All worlds collapse.", boss_class="FINAL CLASS",
         palette=("#110D05","#FFC42D","#E9A400","#7B5100","#1B1B1B"),
         obstacles=["rotating arena rings","lightning spokes","energy walls","moving pylons","collapse zones"],
         speed=["35% outer-ring accelerator","70% inner-ring war-speed launch"],
         boss_abilities=["Universe Collapse (Screen Wide)","Multi-Phase Attack (Changes Form)","Nexus Laser Crown","Arena-Ring Shockwave"],
         hero="Mirror black + championship gold + electric-blue core lines; 3000 Studios thunder crest across wings."),
]

# Route waypoints
events = [
    (3, "SPAWN / LOADOUT LOCK"),
    (12, "WAVE A"),
    (24, "OBSTACLE GATE"),
    (40, "BOOST ZONE"),
    (53, "ELITE WAVE"),
    (65, "WEATHER ESCALATION"),
    (76, "WAR-SPEED SPOT"),
    (85, "MINIBOSS / CHECKPOINT"),
    (90, "PERFECT-RUN WORMHOLE"),
    (100, "BOSS ARENA"),
]

def crop_stage_panel(stage_num):
    for (a,b), path in sheets.items():
        if a <= stage_num <= b and path.exists():
            try:
                img = Image.open(path).convert("RGB")
                w, h = img.size
                idx = stage_num - a
                
                # Precise 3 columns x 2 rows crop across all 4 sheets
                col = idx % 3
                row = idx // 3
                top_margin = int(h * 0.086)
                bottom_margin = int(h * 0.02)
                usable_h = h - top_margin - bottom_margin
                cell_w = w / 3
                cell_h = usable_h / 2
                x0 = int(col * cell_w + 4)
                x1 = int((col+1) * cell_w - 4)
                y0 = int(top_margin + row * cell_h + 3)
                y1 = int(top_margin + (row+1) * cell_h - 3)
                return img.crop((x0,y0,x1,y1))
            except Exception as e:
                print(f"Error cropping panel for stage {stage_num}: {e}")
    
    # Procedural preview panel fallback
    s_item = next(s for s in stages if s["n"] == stage_num)
    thumb = Image.new("RGB", (770, 600), rgb(s_item["palette"][0]))
    tdraw = ImageDraw.Draw(thumb)
    for y in range(600):
        t = y / 600
        c = blend(rgb(s_item["palette"][0]), rgb(s_item["palette"][1]), t * 0.4)
        tdraw.line((0, y, 770, y), fill=c)
    tdraw.rounded_rectangle((10, 10, 760, 590), radius=16, outline=rgb(s_item["palette"][1]), width=4)
    tdraw.text((40, 200), f"STAGE {stage_num:02d}: {s_item['name']}", font=F_H1, fill=rgb(s_item["palette"][1]))
    tdraw.text((40, 260), f"WEATHER: {s_item['weather']}", font=F_H2, fill=rgb(s_item["palette"][2]))
    tdraw.text((40, 320), f"BOSS: {s_item['boss']}", font=F_BODY, fill=(255, 255, 255))
    return thumb

def rgb(hexv):
    hexv = hexv.lstrip("#")
    return tuple(int(hexv[i:i+2],16) for i in (0,2,4))

def blend(a,b,t):
    return tuple(int(a[i]*(1-t)+b[i]*t) for i in range(3))

def wrap(draw, text, font, maxw):
    words=text.split()
    lines=[]; cur=""
    for word in words:
        test=(cur+" "+word).strip()
        if draw.textbbox((0,0), test, font=font)[2] <= maxw:
            cur=test
        else:
            if cur: lines.append(cur)
            cur=word
    if cur: lines.append(cur)
    return lines

specs = []
print(f"Generating 24 Full Stage Blueprints with all 4 official 3000 Studios reference sheets...")

for s in stages:
    W,H = 2048,3072
    base = rgb(s["palette"][0])
    accent = rgb(s["palette"][1])
    accent2 = rgb(s["palette"][2])
    canvas = Image.new("RGB",(W,H),base)
    draw = ImageDraw.Draw(canvas)

    # Background vertical gradient
    for y in range(H):
        t=y/H
        c=blend(base, blend(base, accent, 0.18), 0.15+0.35*t)
        draw.line((0,y,W,y),fill=c)

    # Neon border
    draw.rounded_rectangle((20,20,W-20,H-20), radius=24, outline=accent, width=7)
    draw.rounded_rectangle((36,36,W-36,H-36), radius=20, outline=accent2, width=2)

    # Header
    draw.text((70,55), f"THUNDER DOME // STAGE {s['n']:02d}", font=F_H1, fill=(245,248,255))
    draw.text((70,110), s["name"], font=F_TITLE, fill=accent)
    draw.text((70,195), f"WEATHER: {s['weather']}   //   BOSS: {s['boss']} ({s['boss_class']})", font=F_H2, fill=(220,230,242))
    draw.text((70,238), f'"{s["boss_quote"]}" • ANTIGRAVITY BUILD BLUEPRINT • VERTICAL MOBILE 3D COMBAT', font=F_SMALL, fill=(170,195,215))

    # Concept art panel
    panel = crop_stage_panel(s["n"])
    panel_box=(70,300,840,900)
    if panel:
        p=panel.copy()
        p.thumbnail((panel_box[2]-panel_box[0] - 16, panel_box[3]-panel_box[1] - 16))
        px=panel_box[0]+(panel_box[2]-panel_box[0]-p.width)//2
        py=panel_box[1]+(panel_box[3]-panel_box[1]-p.height)//2
        draw.rounded_rectangle(panel_box, radius=20, fill=(5,7,12), outline=accent, width=4)
        canvas.paste(p,(px,py))
    draw.text((90,860),"CINEMATIC CONCEPT REFERENCE",font=F_TINY,fill=(230,230,240))

    # Core rules/info right of concept
    info_x=900; info_y=310
    draw.text((info_x,info_y),"STAGE IMPLEMENTATION",font=F_H2,fill=accent2)
    info = [
        "Camera: top-down vertical scroll; gameplay lane remains readable.",
        "Layering: sky/weather → far props → mid hazards → ground → near FX.",
        "Mobile target: 60 FPS minimum; use LOD, pooled particles, instancing.",
        "Boss arena begins after 100% route threshold; lock scrolling on entry.",
        "90% wormhole is hidden unless NO DAMAGE + ALL ENEMIES KILLED.",
    ]
    y=info_y+50
    for line in info:
        wrapped=wrap(draw,"• "+line,F_BODY,1040)
        for ln in wrapped:
            draw.text((info_x,y),ln,font=F_BODY,fill=(235,240,248)); y+=31
        y+=8

    # Hero skin box
    hero_y=655
    draw.rounded_rectangle((900,hero_y,1975,900),radius=18,fill=(7,10,15),outline=accent2,width=3)
    draw.text((930,hero_y+22),"HERO PLANE SKIN / GRAPHICS",font=F_H2,fill=accent2)
    yy=hero_y+70
    for ln in wrap(draw,s["hero"],F_BODY,990):
        draw.text((930,yy),ln,font=F_BODY,fill=(235,238,245)); yy+=32
    draw.text((930,hero_y+178),"Texture rules: 4K master • PBR metal/roughness • emissive mask • no baked lighting.",font=F_SMALL,fill=(180,195,212))

    # Main route map
    map_x0,map_y0,map_x1,map_y1=70,960,1280,2900
    draw.rounded_rectangle((map_x0,map_y0,map_x1,map_y1),radius=22,fill=(3,6,10),outline=accent,width=4)
    draw.text((95,map_y0+20),"FULL STAGE ROUTE — 0% → 100%",font=F_H2,fill=accent)

    # Path with serpentine curve
    path=[]
    inner_top=map_y0+110; inner_bottom=map_y1-80
    for i in range(181):
        pct=i/180
        y=inner_top+(inner_bottom-inner_top)*pct
        x=(map_x0+map_x1)//2 + math.sin(pct*math.pi*4.5 + s["n"]*.33)*270 + math.sin(pct*math.pi*11)*40
        path.append((x,y))
    # draw outer lanes and center energy lane
    for offset,wid,col in [(-120,3,blend(accent,(255,255,255),0.25)),(120,3,blend(accent2,(255,255,255),0.2)),(0,10,accent)]:
        pts=[(x+offset,y) for x,y in path]
        draw.line(pts,fill=col,width=wid,joint="curve")

    # Side obstacle fields
    for j in range(26):
        pct=(j+1)/28
        y=inner_top+(inner_bottom-inner_top)*pct
        side=-1 if j%2==0 else 1
        cx=(map_x0+map_x1)//2 + side*(410 + 50*math.sin(j))
        r=18+(j*7%22)
        col=accent2 if j%3 else accent
        draw.regular_polygon((cx,y,r),n_sides=6,rotation=j*9,fill=blend(col,(0,0,0),0.35),outline=col)
        if j%5==0:
            draw.line((cx-r-18,y,cx+r+18,y),fill=col,width=3)

    # Route event nodes
    for pct,label in events:
        t=pct/100
        idx=min(len(path)-1,max(0,int(t*(len(path)-1))))
        x,y=path[idx]
        is_worm=pct==90
        col=(255,210,40) if is_worm else accent2
        rr=22 if is_worm else 14
        draw.ellipse((x-rr,y-rr,x+rr,y+rr),fill=col,outline=(255,255,255),width=3)
        right = ((pct//10)%2==0)
        tx = x+45 if right else max(map_x0+20,x-330)
        boxw=290
        if is_worm: boxw=360
        by=y-25
        draw.rounded_rectangle((tx,by,tx+boxw,by+55),radius=9,fill=(8,10,15),outline=col,width=2)
        draw.text((tx+12,by+12),f"{pct:02d}% {label}",font=F_TINY,fill=(255,255,255))
        draw.line((x+rr if right else x-rr,y,tx if right else tx+boxw,y),fill=col,width=2)

    # Bottom of map labels
    draw.text((95,map_y1-55),"SCROLL AXIS ↓  • lane width varies ±15% for pressure pacing • never hide collision silhouettes",font=F_TINY,fill=(170,188,205))

    # Right details
    rx=1330; ry=990
    sections=[
        ("OBSTACLES / HAZARDS",s["obstacles"],accent),
        ("BOOST + WAR-SPEED",s["speed"],accent2),
        ("BOSS SHIP / POWERS",s["boss_abilities"],(255,120,80)),
    ]
    for title,items,col in sections:
        boxh=360 if title=="OBSTACLES / HAZARDS" else 315
        draw.rounded_rectangle((rx,ry,1975,ry+boxh),radius=18,fill=(7,9,14),outline=col,width=3)
        draw.text((rx+25,ry+22),title,font=F_H2,fill=col)
        yy=ry+72
        for it in items:
            lines=wrap(draw,"• "+it,F_BODY,580)
            for ln in lines:
                draw.text((rx+28,yy),ln,font=F_BODY,fill=(236,239,245)); yy+=31
            yy+=8
        ry+=boxh+35

    # Wormhole requirements box
    worm_y=ry
    draw.rounded_rectangle((rx,worm_y,1975,worm_y+390),radius=18,fill=(16,12,5),outline=(255,210,40),width=4)
    draw.text((rx+25,worm_y+22),"90% SECRET WORMHOLE",font=F_H2,fill=(255,215,50))
    worm_lines=[
        "Spawn only when stageDamageTaken == 0",
        "AND enemiesKilled == enemiesSpawned",
        "Entrance appears at 90% route progress for 4.0 s.",
        "Visual: gold/blue torus gate + lightning filaments + screen-space lensing.",
        "Entry: magnetize player toward center only after explicit overlap; never auto-steal control.",
        "Reward: bonus route / prestige currency / cosmetic roll; server validates multiplayer reward.",
    ]
    yy=worm_y+72
    for it in worm_lines:
        for ln in wrap(draw,"• "+it,F_SMALL,580):
            draw.text((rx+28,yy),ln,font=F_SMALL,fill=(245,240,220)); yy+=28
        yy+=5

    # Build directives box bottom right
    by=worm_y+425
    draw.rounded_rectangle((rx,by,1975,2860),radius=18,fill=(7,9,14),outline=(170,185,205),width=2)
    draw.text((rx+25,by+20),"ANTIGRAVITY BUILD DIRECTIVES",font=F_H2,fill=(220,226,236))
    directives=[
        "Use this blueprint as geometry/encounter timing source-of-truth.",
        "Do not replace existing Kotlin architecture; extend existing catalogs/systems.",
        "Use procedural/reusable mesh kits; no giant unique texture for entire map.",
        "Boss silhouette must match stage palette but stay readable against weather.",
        "Plane graphics use separate albedo/normal/metal/rough/emissive masks.",
        "Validate collision, touch readability, reduced-motion mode, and 60 FPS budget.",
    ]
    yy=by+68
    for it in directives:
        for ln in wrap(draw,"• "+it,F_SMALL,580):
            draw.text((rx+28,yy),ln,font=F_SMALL,fill=(226,232,240)); yy+=27
        yy+=5

    # Footer
    draw.text((70,2960),f"3000 STUDIOS • THUNDER DOME • STAGE {s['n']:02d} • ANTIGRAVITY 3D CONSTRUCTION SPEC",font=F_SMALL,fill=(205,215,228))
    draw.text((70,2995),"Generated from the supplied Thunder Dome concept sheets; route is a build blueprint, not final texture art.",font=F_TINY,fill=(150,165,180))

    # Save to both workspace and artifact output
    fn_name = f"TD_STAGE_{s['n']:02d}_{s['name'].replace(' ','_')}_FULL_LAYOUT.png"
    canvas.save(OUT / fn_name, optimize=True)
    canvas.save(ARTIFACT_OUT / fn_name, optimize=True)
    print(f"  Saved Stage {s['n']:02d}: {s['name']}")
    
    specs.append({
        "stage": s["n"], "name": s["name"], "weather": s["weather"], "boss": s["boss"], "boss_quote": s["boss_quote"], "boss_class": s["boss_class"],
        "palette": s["palette"], "obstacles": s["obstacles"], "boost_war_speed": s["speed"],
        "boss_abilities": s["boss_abilities"], "hero_plane_skin": s["hero"],
        "route_events": [{"percent":p,"event":lab} for p,lab in events],
        "wormhole_unlock": {"at_percent":90,"requires_no_damage":True,"requires_all_enemies_killed":True,"appearance_seconds":4.0}
    })

# Write machine-readable build spec and README
spec_json = json.dumps({
    "project":"THUNDER DOME",
    "studio":"3000 Studios",
    "target":"Android / Google Play",
    "camera":"Top-down vertical mobile scroller",
    "shared_rules":{
        "wormhole":"At 90% only if no damage and all enemies killed",
        "performance":"60 FPS minimum on target Android devices",
        "textures":"PBR; 4K masters for hero/boss assets; runtime-scaled as needed",
        "collision":"Keep hazard silhouettes readable; never obscure hitboxes with FX",
        "multiplayer":"Server-authoritative validation for score/reward-critical events"
    },
    "stages":specs
}, indent=2)

(OUT/"thunder_dome_24_stage_build_spec.json").write_text(spec_json, encoding="utf-8")
(ARTIFACT_OUT/"thunder_dome_24_stage_build_spec.json").write_text(spec_json, encoding="utf-8")

readme = """# Thunder Dome — 24 Full Stage Layout Blueprints

This package contains 24 full-size 2048×3072 annotated PNG construction blueprints for Google Antigravity / human 3D implementation.

Each image includes:
- supplied concept-art reference panel
- full 0%→100% vertical route
- obstacle/hazard zones
- boost and war-speed markers
- weather escalation
- miniboss/checkpoint placement
- a 90% perfect-run wormhole condition
- boss ship abilities
- hero-plane graphic/skin direction
- mobile optimization and implementation rules

The JSON file is the machine-readable source for the same stage facts.

Perfect-run wormhole rule:
`stageProgress >= 0.90 && damageTaken == 0 && enemiesKilled == enemiesSpawned`

These are construction blueprints, not final in-game raster backgrounds. Build stages from reusable 3D kits, procedural props, decals, PBR materials, and pooled FX.
"""
(OUT/"README.md").write_text(readme, encoding="utf-8")
(ARTIFACT_OUT/"README.md").write_text(readme, encoding="utf-8")

zip_path = Path(r"C:\Users\MrJws\OneDrive\Workspaces\Thunder Dome\Thunder_Dome_24_Full_Stage_Layouts_Antigravity.zip")
with zipfile.ZipFile(zip_path, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=6) as z:
    for p in sorted(OUT.iterdir()):
        z.write(p, arcname=p.name)

# Also copy zip to artifacts
shutil.copy(zip_path, ARTIFACT_OUT.parent / "Thunder_Dome_24_Full_Stage_Layouts_Antigravity.zip")

print(f"\nSUCCESS: Created {len(stages)} annotated full-stage PNGs with all 4 official 3000 Studios storyboard sheets.")
print(f"Archive saved to: {zip_path}")
