// Selco: edit a key's text in every language at once (the search shows one column per language). Module and keycode
// are read-only; each language gets a text box prefilled with its current text, named message_<locale code>.
const getEditModalConfig = ({
    t,
    editRow,
    languages = []
}) => {

    return {
        label: {
            heading: `WBH_LOC_EDIT_LOCALISATION`,
            submit: `WBH_LOC_EDIT_MODAL_SUBMIT`,
            cancel: "WBH_LOC_EDIT_MODAL_CANCEL",
        },
        form: [
            {
                body: [
                  {
                    label: t("WBH_LOC_MODULE"),
                    type: "text",
                    disable:true,
                    populators: {
                        name: "module",
                        defaultValue:editRow.module,
                    },
                  },
                  {
                    label: t("WBH_LOC_CODE"),
                    type: "text",
                    disable:true,
                    populators: {
                        name: "code",
                        defaultValue:editRow.code,
                    },
                  },
                  ...languages.map((language) => ({
                    label: language.label,
                    type: "text",
                    disable:false,
                    populators: {
                        name: `message_${language.code}`,
                        defaultValue: editRow.messagesByLocale?.[language.code] || "",
                    },
                  })),
                ]
            }
        ]
    }
}

export default getEditModalConfig
