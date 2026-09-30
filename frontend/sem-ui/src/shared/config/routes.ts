import { contextPath } from "./global-config";

export function employeeHomePath(): string {
  return `/${contextPath()}/employee`;
}

export function employeeLoginPath(): string {
  return `/${contextPath()}/employee/user/login`;
}

export function employeeForgotPasswordPath(): string {
  return `/${contextPath()}/employee/user/forgot-password`;
}

export function employeeChangePasswordPath(): string {
  return `/${contextPath()}/employee/user/change-password`;
}

export function employeeProfilePath(): string {
  return `/${contextPath()}/employee/profile`;
}

export function employeeProfileChangePasswordPath(): string {
  return `/${contextPath()}/employee/profile/change-password`;
}
