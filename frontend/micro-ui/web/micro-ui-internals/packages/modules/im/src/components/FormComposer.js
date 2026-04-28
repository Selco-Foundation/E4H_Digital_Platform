import React, { useMemo } from "react";
import { useForm } from "react-hook-form";
import {
  BreakLine,
  Card,
  CardLabel,
  CardLabelError,
  CardSubHeader,
  CardSectionHeader,
  TextArea,
  TextInput,
  ActionBar,
  SubmitBar,
  LabelFieldPair,
  Header,
  Button,
} from "@selco/digit-ui-react-components";

import { useTranslation } from "react-i18next";

export const FormComposer = (props) => {
  console.log("props999", props)
  const mobileDeviceWidth = 780;
  const [isMobileView, setIsMobileView] = React.useState(window.innerWidth <= mobileDeviceWidth);
  const onResize = () => {
    if (window.innerWidth <= mobileDeviceWidth) {
      if (!isMobileView) {
        setIsMobileView(true);
      }
    } else {
      if (isMobileView) {
        setIsMobileView(false);
      }
    }
  };
  React.useEffect(() => {
    window.addEventListener("resize", () => {
      onResize();
    });
    return () => {
      window.addEventListener("resize", () => {
        onResize();
      });
    };
  });
  console.log("isMobileView", isMobileView)
  const { register, handleSubmit, errors } = useForm();
  const { t } = useTranslation();

  function onSubmit(data) {
    props.onSubmit(data);
  }

  const fieldSelector = (type, populators) => {
    switch (type) {
      case "text":
        return (
          <div className="field-container">
            {populators.componentInFront ? populators.componentInFront : null}
            <TextInput className="field desktop-w-full" {...populators} inputRef={register(populators.validation)} />
          </div>
        );
      case "date":
        return (
          <div className="field-container">
            {populators.componentInFront ? populators.componentInFront : null}
            <TextInput type={"date"} className="field desktop-w-full" {...populators} inputRef={register(populators.validation)} />
          </div>
        );
      case "textarea":
        return <TextArea className="field desktop-w-full" name={populators.name || ""} {...populators} inputRef={register(populators.validation)} />;
      default:
        return populators.dependency !== false ? populators : null;
    }
  };

  const formFields = useMemo(
    () =>
      props.config?.map((section, index, array) => {
        return (
          
          <React.Fragment key={index}>
            <CardSectionHeader>{section.head}</CardSectionHeader>
            {section.body.map((field, index) => {
              return (
                <React.Fragment key={index}>
                  
                  <LabelFieldPair>
                    <CardLabel>
                      {field.label}
                      {field.isMandatory ? " * " : null}
                    </CardLabel>
                    <div className="field">{fieldSelector(field.type, field.populators)}</div>
                  </LabelFieldPair>
                  {errors[field.populators.name] && (field.populators?.validate ? errors[field.populators.validate] : true) && (
                    <CardLabelError>{field.populators.error}</CardLabelError>
                  )}
                </React.Fragment>
              );
            })}
            {array.length - 1 === index ? null : <BreakLine />}
          </React.Fragment>
         
        );
      }),
    [props.config, errors]
  );

  const isDisabled = props.isDisabled || false;

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <Card>
        <div style={{ marginLeft: isMobileView ? "-15px" : "0px" }}>
          <Header>{props.heading || t("NEW_TICKET")}</Header>
        </div>
        {formFields}
        {props.children}
        <ActionBar className={props?.actionBarClassName}>
          <SubmitBar disabled={isDisabled} label={t(props.label)} submit="submit" />
          {props.secondaryActionLabel && (
            <Button name="secondary-action" className="previous-button" variation="secondary" label={t(props?.secondaryActionLabel)} onButtonClick={props?.onSecondaryActionClick} />
          )}
        </ActionBar>
      </Card>
    </form>
  );
};
