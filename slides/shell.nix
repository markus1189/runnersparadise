{ pkgs ? import <nixpkgs> {} }: with pkgs;

let
  tex = texlive.combine {
    inherit (texlive)
    animate
    babel
    beamer
    chngcntr
    cleveref
    exercise
    enumitem
    etoolbox
    excludeonly
    fancyvrb
    float
    framed
    ifplatform
    lineno
    listings
    mdframed
    media9
    microtype
    minted
    needspace
    ocgx2
    pgf
    scheme-medium
    todonotes
    upquote
    xcolor
    xcolor-solarized
    xstring;
  };
in
stdenv.mkDerivation {
  name = "final-encoding";
  buildInputs = [
    tex
    which
  ] ++ (with pythonPackages; [
    pygments
  ]);
}
