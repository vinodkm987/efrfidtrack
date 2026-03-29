"""
Patches Zebra RFID API3 AARs that share the same manifest namespace (com.zebra.rfid.api3).
AGP 9.x requires each library to have a unique namespace.
These AARs contain only Java classes with no Android resources, so changing the
manifest package attribute is safe and does not affect class imports or functionality.
Patched AARs are written to app/libs/.
"""
import zipfile, shutil, os

SRC = "RFIDAPI3Library"
DST = "app/libs"
os.makedirs(DST, exist_ok=True)

# AARs that share com.zebra.rfid.api3 — give each a unique sub-namespace
TO_PATCH = [
    ("API3_CMN-release-2.0.5.238.aar",       "com.zebra.rfid.api3.cmn"),
    ("API3_INTERFACE-release-2.0.5.238.aar",  "com.zebra.rfid.api3.iface"),
    ("API3_TRANSPORT-release-2.0.5.238.aar",  "com.zebra.rfid.api3.transport"),
    ("API3_ASCII-release-2.0.5.238.aar",      "com.zebra.rfid.api3.ascii"),
]

# AARs that already have a unique namespace — copy as-is
TO_COPY = [
    "API3_READER-release-2.0.5.238.aar",  # keeps com.zebra.rfid.api3
    "rfidhostlib.aar",
    "rfidseriallib.aar",
]

for aar_name, new_pkg in TO_PATCH:
    src = os.path.join(SRC, aar_name)
    dst = os.path.join(DST, aar_name)
    with zipfile.ZipFile(src, "r") as zin:
        with zipfile.ZipFile(dst, "w", compression=zipfile.ZIP_DEFLATED) as zout:
            for item in zin.infolist():
                data = zin.read(item.filename)
                if item.filename == "AndroidManifest.xml":
                    data = data.replace(
                        b'package="com.zebra.rfid.api3"',
                        f'package="{new_pkg}"'.encode()
                    )
                zout.writestr(item, data)
    print(f"Patched : {aar_name}  ->  {new_pkg}")

for aar_name in TO_COPY:
    shutil.copy(os.path.join(SRC, aar_name), os.path.join(DST, aar_name))
    print(f"Copied  : {aar_name}")

print("\nDone. app/libs/ is ready.")
