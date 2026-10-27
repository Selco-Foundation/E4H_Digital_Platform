import re


def clean_boundary_name(raw) -> str:
    """The name a boundary code is built from - the same cleaning as the LGD onboarding
    script's clean_name (scripts/lgd_common.py), so a sheet row and the onboarding build the
    same code for the same place and the upload finds it instead of creating a duplicate.

    Brackets become spaces (keeping the qualifier: 'Kakinada (Rural)' -> 'Kakinada Rural'),
    '&' becomes 'and', dots, apostrophes and '*' are dropped ('G.Madugula' -> 'GMadugula'),
    ',', '_' and '/' become spaces, a leading 'The ' is removed, and a name typed in capitals
    is title-cased unless it looks like an initialism ('K G F'). Hyphens are kept; the code
    builders treat them as word breaks. Only the code uses this - the label keeps the name as
    typed.
    """
    name = re.sub(r"[()\[\]]", " ", str(raw).strip())
    name = name.replace("&", " and ").replace(".", "").replace("'", "").replace("*", "")
    name = re.sub(r"[,_/]", " ", name)
    name = re.sub(r"\s+", " ", name).strip()
    if name.isupper() and all(len(word) > 3 for word in name.split()):
        name = name.title()
    return re.sub(r"^The\s+", "", name)


def boundary_code_segment(text) -> str:
    """One level of an E4H boundary code: the cleaned name's words, each with its first
    letter upper-cased and the rest kept, concatenated - 'Tamil Nadu' -> 'TamilNadu',
    'Kakinada (Rural)' -> 'KakinadaRural'. '_' and '-' are word breaks. The LGD onboarding
    script builds e4h codes the same way."""
    if text is None or not str(text).strip():
        return ""
    cleaned = re.sub(r"[_\-]+", " ", clean_boundary_name(text))
    return "".join(word[:1].upper() + word[1:] for word in cleaned.split())
