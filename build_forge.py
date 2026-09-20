import os
import sys
import subprocess
import shutil
import zipfile

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

    # 3. Create JAR archive
    print(f"[Forge 1.20.1] Building JAR: {out_jar}")
    cmd_jar = [jar_path, '--create', '--file', out_jar, '-C', bin_dir, '.']
    res_jar = subprocess.run(cmd_jar, capture_output=True, text=True)
    if res_jar.returncode != 0:
        print("[Forge 1.20.1] Jar creation failed!")
        print(res_jar.stderr)
        return False

    print(f"[Forge 1.20.1] SUCCESS! Created: {out_jar} ({os.path.getsize(out_jar)} bytes)")
    return True

if __name__ == '__main__':
    if not build_forge():
        sys.exit(1)
