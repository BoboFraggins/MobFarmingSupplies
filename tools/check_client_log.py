#!/usr/bin/env python3
"""Lists the problems in a dev client's game log (tools/test-forge.sh runs it after the smoke test).

Flags errors, exceptions, missing models/textures/sounds and late registrations, minus the noise
every dev run has (listed in NOISE with the reason). Exit status 1 if anything is left.

Usage: python3 tools/check_client_log.py <latest.log>
"""
import re, sys

FLAGS = re.compile(r'/ERROR\]|/ERROR\)|Exception|Missing (model|texture|sound)|Unable to load|'
                   r'missing texture|Couldn\'t|registered after event|Failed to', re.I)

NOISE = [
    (r'glfwInit took', 'early display timing'),
    (r'is missing mods\.toml file', "Forge's own language/library jars in dev"),
    (r'mixin\).*Error loading class: mezz/jei', "JEI mixin target that isn't in this JEI version"),
    (r'Missing sound for event: minecraft:(item\.goat_horn|entity\.goat)', 'vanilla dev assets'),
    (r'Missing data pack mod:generated_|generated_\w+ \(version', "Loom's generated dev pack"),
    (r"Unable to parse the boolean system property 'java\.net\.preferIPv6Addresses'", 'netty'),
    (r'Unable to load item from: .*dna_booster_pack_(aether|aquaculture|evilcraft)',
     'booster packs for optional mods, registered only when those mods are present'),
    (r'Failed to (verify|fetch) .*(authentication|profile|realms|Realms)', 'offline dev account'),
    (r'Realms|realms', 'offline dev account'),
    (r'YggdrasilAuthenticationService|com\.mojang\.authlib', 'offline dev account'),
    (r'Reflective setAccessible\(true\) disabled|jdk\.internal\.misc\.Unsafe|io\.netty', 'netty probing the JVM'),
    (r'Forge Version Check', 'version check'),
]


def main(path):
    lines = open(path, errors='replace').read().split('\n')
    problems = []
    for i, line in enumerate(lines):
        if not FLAGS.search(line):
            continue
        if any(re.search(pattern, line) for pattern, _ in NOISE):
            continue
        # Keep stack traces short: the line plus the first frames.
        frames = [l.strip() for l in lines[i + 1:i + 4] if l.strip().startswith('at ')]
        problems.append(line.strip()[:300] + ''.join('\n      ' + f[:200] for f in frames))
    seen, unique = set(), []
    for p in problems:
        key = re.sub(r'^\[[^\]]*\]\s*', '', p.split('\n')[0])
        if key not in seen:
            seen.add(key)
            unique.append(p)
    for p in unique:
        print('  - ' + p)
    print(f'{len(unique)} problem(s) in {path}')
    return 1 if unique else 0


if __name__ == '__main__':
    sys.exit(main(sys.argv[1]))
