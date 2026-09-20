import os
import sys
import subprocess
import shutil
import zipfile
import glob

base_dir = os.path.dirname(os.path.abspath(__file__))

javac_path = r'C:\Program Files\Eclipse Adoptium\jdk-21.0.12.8-hotspot\bin\javac.exe'
jar_path = r'C:\Program Files\Eclipse Adoptium\jdk-21.0.12.8-hotspot\bin\jar.exe'

if not os.path.exists(javac_path):
    javac_path = 'javac'
    jar_path = 'jar'

def build_forge():
    print("==================================================")
    print("  BUILDING TARGET: FORGE 1.20.1")
    print("==================================================")

    out_dir = os.path.join(base_dir, 'forge', 'build', 'libs')
    os.makedirs(out_dir, exist_ok=True)
    out_jar = os.path.join(out_dir, "Iron's Spell Tweaker GUI-Forge-1.20.1-1.0.0.jar")

    bin_dir = os.path.join(base_dir, 'forge', 'build', 'classes')
    if os.path.exists(bin_dir):
        shutil.rmtree(bin_dir)
    os.makedirs(bin_dir, exist_ok=True)

    libs_cp = os.path.join(base_dir, 'libs_forge', '*')
    src_dirs = [
        os.path.join(base_dir, 'common', 'src', 'main', 'java'),
        os.path.join(base_dir, 'forge', 'src', 'main', 'java')
    ]
    res_dirs = [
        os.path.join(base_dir, 'common', 'src', 'main', 'resources'),
        os.path.join(base_dir, 'forge', 'src', 'main', 'resources')
    ]

    # 1. Collect Java files
    java_files = []
    for s_dir in src_dirs:
        for root, _, files in os.walk(s_dir):
            for f in files:
                if f.endswith('.java'):
                    java_files.append(os.path.join(root, f))

    print(f"Compiling {len(java_files)} Java files targeting Java 17...")
    cmd = [javac_path, '-source', '17', '-target', '17', '-cp', libs_cp, '-d', bin_dir] + java_files
    res = subprocess.run(cmd, capture_output=True, text=True)
    if res.returncode != 0:
        print("[Forge 1.20.1] Compilation failed!")
        print(res.stderr)
        return False
    print("[Forge 1.20.1] Compilation SUCCESSFUL!")

    # 2. Copy resources
    print("[Forge 1.20.1] Copying resources...")
    for r_dir in res_dirs:
        if os.path.exists(r_dir):
            for root, _, files in os.walk(r_dir):
                rel = os.path.relpath(root, r_dir)
                target_root = os.path.join(bin_dir, rel) if rel != '.' else bin_dir
                os.makedirs(target_root, exist_ok=True)
                for f in files:
                    src = os.path.join(root, f)
                    dst = os.path.join(target_root, f)
                    shutil.copy2(src, dst)

    # 3. Create initial JAR archive
    print(f"[Forge 1.20.1] Packaging initial JAR: {out_jar}")
    cmd_jar = [jar_path, '--create', '--file', out_jar, '-C', bin_dir, '.']
    res_jar = subprocess.run(cmd_jar, capture_output=True, text=True)
    if res_jar.returncode != 0:
        print("[Forge 1.20.1] Jar creation failed!")
        print(res_jar.stderr)
        return False

    # 4. Remap bytecode from Mojang (named) mappings to SRG for Forge 1.20.1 runtime
    user_home = os.path.expanduser("~")
    gradle_cache = os.path.join(user_home, ".gradle", "caches")
    mappings_pattern = os.path.join(gradle_cache, "fabric-loom", "1.20.1", "*", "mappings-srg.tiny")
    mappings_matches = glob.glob(mappings_pattern)
    mappings_file = mappings_matches[0] if mappings_matches else ""

    tiny_pattern = os.path.join(gradle_cache, "modules-2", "files-2.1", "net.fabricmc", "tiny-remapper", "*", "*", "*.jar")
    tiny_matches = [p for p in glob.glob(tiny_pattern) if not p.endswith("-sources.jar")]
    mapping_io_pattern = os.path.join(gradle_cache, "modules-2", "files-2.1", "net.fabricmc", "mapping-io", "*", "*", "*.jar")
    mapping_io_matches = [p for p in glob.glob(mapping_io_pattern) if not p.endswith("-sources.jar")]
    asm_pattern = os.path.join(gradle_cache, "modules-2", "files-2.1", "org.ow2.asm", "*", "9.*", "*", "*.jar")
    asm_matches = [p for p in glob.glob(asm_pattern) if not p.endswith("-sources.jar") and not p.endswith("-javadoc.jar")]
    loom_forge_pattern = os.path.join(gradle_cache, "fabric-loom", "minecraftMaven", "net", "minecraft", "forge-1.20.1-47.4.20-minecraft-merged", "*", "*.jar")
    loom_forge_matches = glob.glob(loom_forge_pattern)
    loom_forge_jar = loom_forge_matches[0] if loom_forge_matches else ""

    if mappings_file and tiny_matches and mapping_io_matches and loom_forge_jar:
        print("Remapping bytecode from Mojang mappings to SRG mappings for Forge 1.20.1...")
        unmapped_jar = os.path.join(out_dir, "unmapped.jar")
        if os.path.exists(unmapped_jar):
            os.remove(unmapped_jar)
        shutil.move(out_jar, unmapped_jar)

        remapper_cp = [tiny_matches[0], mapping_io_matches[0]] + asm_matches
        remap_cmd = [
            "java",
            "-cp", ";".join(remapper_cp),
            "net.fabricmc.tinyremapper.Main",
            unmapped_jar,
            out_jar,
            mappings_file,
            "named",
            "srg",
            loom_forge_jar,
            os.path.join(base_dir, 'libs_forge', 'irons_spellbooks-1.20.1-3.16.2.jar')
        ]
        remap_res = subprocess.run(remap_cmd, capture_output=True, text=True)
        if remap_res.returncode != 0:
            print("Remap failed, falling back to unmapped jar:")
            print(remap_res.stderr)
            shutil.copyfile(unmapped_jar, out_jar)
        else:
            print(f"[Forge 1.20.1] Remap to SRG SUCCESSFUL! Created: {out_jar} ({os.path.getsize(out_jar)} bytes)")
            if os.path.exists(unmapped_jar):
                os.remove(unmapped_jar)
    else:
        print("Warning: Tiny-remapper or SRG mappings not found; using unmapped JAR.")

    print(f"\n[Forge 1.20.1] SUCCESS! Created: {out_jar} ({os.path.getsize(out_jar)} bytes)")
    return True

if __name__ == '__main__':
    if not build_forge():
        sys.exit(1)
