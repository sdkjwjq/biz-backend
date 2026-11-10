// Read the property itself so Vue tracks initially absent keys as well as true/false.
// Object.hasOwn() does not create that reactive dependency on an empty overrides map.
export function permissionOverrideValue(overrides, code) {
  const value = overrides[code]
  return value === true ? 'allow' : value === false ? 'deny' : 'inherit'
}
