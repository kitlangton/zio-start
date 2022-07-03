const colors = require('tailwindcss/colors')
const typography = require('@tailwindcss/typography')
const forms = require('@tailwindcss/forms')
const scalaVersion = require('./scala-version')
const path = require('path')
const scalajsMode = process.env.NODE_ENV === 'production' === 'production' ? 'opt' : 'fastopt'

module.exports = {
    content: [
        "./frontend/**/*.{js, scala}",
        "./index.html"
    ],
    theme: {
        extend: {
            fontFamily: {
                serif: ['Inter', 'ui-serif', 'Georgia', 'Cambria', '"Times New Roman"', 'Times', 'serif'],
            },
            colors: {
                gray: colors.stone,
                orange: colors.orange,
            },
        },
    },
    variants: {
        extend: {
            transitionDuration: ['hover']
        }
    },
    corePlugins: {},
    plugins: [typography, forms],
}