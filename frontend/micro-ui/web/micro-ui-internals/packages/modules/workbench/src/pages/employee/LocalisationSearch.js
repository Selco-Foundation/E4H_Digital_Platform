import { AddFilled, Button, Header, InboxSearchComposer, WorkflowModal,ActionBar,SubmitBar, InfoBanner } from "@egovernments/digit-ui-react-components";
import { Toast } from "@egovernments/digit-ui-components";
import React, { useState, useEffect, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useParams } from "react-router-dom";
import _, { drop } from "lodash";
import { Config } from "../../configs/LocalisationSearchConfig";
import getEditModalConfig from "../../configs/EditModalConfig";
import { useQueryClient } from "react-query";
import { AlertCard } from "@egovernments/digit-ui-components";

const LocalisationSearch = () => {
  const { t } = useTranslation();
  const queryClient = useQueryClient()
  const history = useHistory();
  const tenantId = Digit.ULBService.getCurrentTenantId();
  const [showToast, setShowToast] = useState(false);
  const [modalConfig, setModalConfig] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [editRow,setEditRow] = useState(null)
  const [formData,setFormData] = useState(null)
  const [callRefetch,setCallRefetch] = useState(false)

  // Selco: every language in common-masters.Languages gets its own result column (English first), instead of a
  // language filter on the search form
  const { data: languages } = Digit.Hooks.useCustomMDMS(Digit.ULBService.getStateId(), "common-masters", [{ name: "Languages" }], {
    select: (data) => {
      const list = (data?.["common-masters"]?.Languages || []).filter((language) => language?.code);
      return [...list.filter((language) => language.code === "en_IN"), ...list.filter((language) => language.code !== "en_IN")];
    },
  });
  const searchConfig = useMemo(() => {
    if (!languages) return null;
    const config = _.cloneDeep(Config);
    const columns = config.sections.searchResult.uiConfig.columns;
    const languageColumns = languages.map((language) => ({ label: language.label, jsonPath: `messagesByLocale.${language.code}` }));
    // after Keycode and Module Name, so the language columns are the part that scrolls sideways
    columns.splice(2, 0, ...languageColumns);
    config.additionalDetails = { ...config.additionalDetails, languages: languages.map((language) => language.code) };
    return config;
  }, [languages]);

  const reqCriteriaAdd = {
    url: `/localization/messages/v1/_upsert`,
    params: {},
    body: {
      tenantId,
    },
    config: {
      enabled: true,
    },
  };

  const mutation = Digit.Hooks.useCustomAPIMutationHook(reqCriteriaAdd);

  const closeToast = () => {
    setTimeout(() => {
      setShowToast(null);
    }, 5000);
  };

  const formUpdate = (form) => {
    setFormData(form)
  }

  const onModalSubmit = async (payload) => {
    if(!payload?.message){
      setShowToast({ label: `${t("WBH_LOC_ENTER_VALID_MESSAGE")}`,type:"error",style:{
        zIndex:"10000"
      } });
      closeToast()
      return
    }
    const onSuccess = (resp) => {
      setShowToast({ label: `${t("WBH_LOC_UPDATE_SUCCESS")}` });
      setShowModal(null)
      setEditRow(null)
      setModalConfig(null)
      // const queryCache = queryClient.getQueryCache()
      // const queryKeys = queryCache.getAll().map(cache => cache.queryKey) // QueryKey[]
      // queryClient.invalidateQueries([`/localization/messages/v1/_upsert`,`Random`])
      // queryClient.invalidateQueries([`/localization/messages/v1/_upsert`,`Random`,`defaultLocale`])
      closeToast();
      setCallRefetch(true)
    };
    const onError = (resp) => {
      let label = `${t("WBH_LOC_UPDATE_FAIL")}: `
      resp?.response?.data?.Errors?.map((err,idx) => {
        if(idx===resp?.response?.data?.Errors?.length-1){
          label = label + t(Digit.Utils.locale.getTransformedLocale(err?.code)) + '.'
        }else{
        label = label + t(Digit.Utils.locale.getTransformedLocale(err?.code)) + ', '
        }
      })
      setShowToast({ label, type:"error" });
      setShowModal(null)
      setEditRow(null)
      closeToast();
    };


    mutation.mutate(
      {
        params: {},
        body: {
          tenantId,
          messages: [payload],
        },
      },
      {
        onError,
        onSuccess,
      }
    );

  }

  const onClickSvg = (row) => {
    setEditRow(row.original)
    setShowModal(true)
  }
  
  useEffect(() => {
    if(editRow) {
      setModalConfig(
        getEditModalConfig({
          t,
          editRow,
          formData
        })
      );
    }
  }, [editRow]);

  return (
    <React.Fragment>
      <div className="jk-header-btn-wrapper">
      <Header className="works-header-search">{t(Config?.label)}</Header>
      {/* {Config && Digit.Utils.didEmployeeHasRole(Config?.actionRole) && (
          <Button
            label={t(Config?.actionLabel)}
            variation="secondary"
            icon={<AddFilled style={{ height: "20px", width: "20px" }} />}
            onButtonClick={() => {
              history.push(`/${window?.contextPath}/employee/${Config?.actionLink}`);
            }}
            type="button"
            className={'header-btn'}
          />
        )} */}
      {
        Config && Digit.Utils.didEmployeeHasAtleastOneRole(Config?.actionRoles) &&
        <ActionBar >
          <SubmitBar disabled={false} className="mdms-add-btn"  onSubmit={() => {
              history.push(`/${window?.contextPath}/employee/${Config?.actionLink}`);
            }} label={t("WBH_ADD_LOCALISATION")} />
        </ActionBar>
      }
      </div>
      <AlertCard additionalElements={[]} label={t("WBH_INFO")} text={t("WBH_INFO_MESSAGE")} variant="default" style={{marginBottom:"1.5rem",maxWidth:"100%"}}/>
      {searchConfig && <div className="inbox-search-wrapper">
        <InboxSearchComposer onFormValueChange={formUpdate} configs={searchConfig} additionalConfig = {{
          resultsTable:{
            onClickSvg
          },
          search:{
            callRefetch,
            setCallRefetch
          }
        }}></InboxSearchComposer>
      </div>}
      {showModal && modalConfig && <WorkflowModal closeModal={() => setShowModal(false)} onSubmit={onModalSubmit} config={modalConfig} popupModuleActionBarStyles={{marginTop:"-1rem"}} popupModuleMianStyles={{marginTop:"-2rem"}} />}
      {showToast && <Toast label={showToast?.label} type={showToast?.type} isDleteBtn={true} onClose={() => setShowToast(null)} style={showToast?.style}></Toast>}
    </React.Fragment>
  );
};

export default LocalisationSearch;
